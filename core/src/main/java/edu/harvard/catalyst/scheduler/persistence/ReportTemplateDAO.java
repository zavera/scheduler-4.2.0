/**
 * Copyright (c) 2015-2016, President and Fellows of Harvard College
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.harvard.catalyst.scheduler.persistence;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.hccrc.core.util.RichIterator;
import edu.harvard.catalyst.hccrc.core.util.RichList;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateMetadataDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateResultDTO;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.hccrc.core.util.Try.attempt;

;

/**
 * @author Bill Simons
 * @date 8/8/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Repository
public class ReportTemplateDAO extends SiteDAO {
    private static final Logger LOGGER = Logger.getLogger(ReportTemplateDAO.class);

    public ReportTemplate findTemplateById(final Integer id) {
        return findById(ReportTemplate.class, id);
    }

    public TemplateUser findTemplateUserById(final Integer id) {
        return findById(TemplateUser.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<TemplateCategory> findCategoriesByTemplate(final ReportTemplate reportTemplate) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateCategory.class);
        criteriaHelper.whereEquals("reportTemplate", reportTemplate);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<SubCategory> findSubCategoriesByCategoryAndTemplate(final Category category, final ReportTemplate reportTemplate) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), SubCategory.class);

        criteriaHelper.whereEquals("parent", category);
        criteriaHelper.whereEquals("reportTemplate", reportTemplate);

        return criteriaHelper.getQuery().list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateCategoryField> findTcfsByCategoryAndTemplate(final Category category, final ReportTemplate reportTemplate) {
        final CriteriaQueryHelper criteriaHelper = new CriteriaQueryHelper(session(), TemplateCategoryField.class);

        criteriaHelper.whereEquals("category", category);
        criteriaHelper.whereEquals("reportTemplate", reportTemplate);

        return criteriaHelper.getQuery().list();
    }

    public ReportTemplateResultDTO runQueryAndScalars(final String reportName,
                                                      final Graph.QueryScalarsTcfs queryScalarsTcfs,
                                                      final Date from,
                                                      final Date to,
                                                      final int offset,
                                                      final int limit) {

        return runQueryAndScalars(reportName, queryScalarsTcfs, from, to,
                offset, limit, false);
    }

    public ReportTemplateResultDTO runQueryAndScalars(final String reportName,
                                                      final Graph.QueryScalarsTcfs queryScalarsTcfs,
                                                      final Date from,
                                                      final Date to,
                                                      final int offset,
                                                      final int limit,
                                                      final boolean countOnly) {

        final ReportTemplateResultDTO reportTemplateResultDTO = new ReportTemplateResultDTO(reportName);

        final List<TemplateCategoryField> qsfTcfs = queryScalarsTcfs.getTcfs();

        // first chunk only
        if (offset == 0) {
            final List<String> headerList = enrich(qsfTcfs).map(tcf -> {
                final Field field = tcf.getField();
                final Category category = tcf.getCategory();

                return category.getCsvName() + " - " + field.getDisplayName();
            }).toList();

            reportTemplateResultDTO.setCsvHeaders(headerList);
        }

        final List<String> qsfScalars = queryScalarsTcfs.getScalars();

        String queryString = makeQueryString(queryScalarsTcfs, from, to, offset, limit, countOnly);

        final NativeQuery sqlQuery = newNativeQuery(queryString);

        addToAndFrom(to, from, sqlQuery);

        if (!countOnly) {
            qsfScalars.forEach(sqlQuery::addScalar);

            if (offset == 0) {
                reportTemplateResultDTO.setCachedQsTcfs(queryScalarsTcfs);
                LOGGER.info("\n--> Running query: " + queryString);
            }
        }

        @SuppressWarnings("unchecked")
        final List<Object> listOfResultSetObjects = sqlQuery.list();

        if (listOfResultSetObjects.isEmpty()) {
            return reportTemplateResultDTO;
        }

        final Object firstResultSet = listOfResultSetObjects.get(0);

        final boolean oneObject = !(firstResultSet instanceof Object[]);

        final Function<Object, List<String>> toCsvRow = resultSetObject -> {
            // if only one field in the query, then each resultSet is one Object
            // if more than one query field, then each resultSet is an Object[]
            final Object[] resultSetAsArray = oneObject ? new Object[] { resultSetObject } : (Object[]) resultSetObject;

            final RichIterator<Field> fields = richIterator(enrich(qsfTcfs).map(tcf -> tcf.getField()));

            final RichIterator<Object> remaining = richIterator(enrich(resultSetAsArray));

            final RichIterator<String> csvFields = fields.map(toCsvField(remaining));

            return csvFields.toList();
        };

        final List<List<String>> csvRows = enrich(listOfResultSetObjects).map(toCsvRow).toList();

        reportTemplateResultDTO.setCsvRows(csvRows);

        return reportTemplateResultDTO;
    }
    
    private static final <A> RichIterator<A> richIterator(final RichList<A> rl) {
        return RichIterator.enrich(rl.iterator());
    }

    private static final Function<Object, Date> toDate = o -> (Date) o;

    private static final Function<Date, Date> toEndOfDay = passNullsThrough(DateUtility::adjustDateToEndOfDay);

    private static final Function<Date, Integer> toMinutes = passNullsThrough(date -> DateUtility.convertMillisToMinutes(date.getTime()));

    Function<Field, String> toCsvField(final RichIterator<Object> remaining) {
        return field -> {
            final FieldProcessingEnum fieldProcessingEnum = field.getFieldProcessingEnum();
            final String columnsString = field.getColumn();
            final List<String> columns = newArrayList(Splitter.on(",").split(columnsString));
            final int numColumns = columns.size();

            final Object rsField;

            if (numColumns == 1) {
                rsField = handleOneColumn(remaining.next(), fieldProcessingEnum);
            } else if (numColumns == 2) {
                rsField = handleTwoColumns(remaining, fieldProcessingEnum);
            } else if (numColumns == 4) {
                rsField = handleFourColumns(remaining);
            } else {
                rsField = "<bad case>";
            }

            return stringOrDateAsString(rsField);
        };
    }

    Integer handleFourColumns(final RichIterator<Object> remaining) {
        // Assume next four objects are all Dates
        final List<Long> timestamps = remaining.take(4).map(toDate).map(passNullsThrough(Date::getTime)).toList();

        if (timestamps.contains(null)) {
            return null;
        } 
          
        final Long difference1 = timestamps.get(0) - timestamps.get(1);
        final Long difference2 = timestamps.get(2) - timestamps.get(3);

        return DateUtility.convertMillisecondsToMinutes(difference1 - difference2);
    }

    Integer handleTwoColumns(final RichIterator<Object> remaining, final FieldProcessingEnum fieldProcessingEnum) {

        // Assume next two objects are both Dates
        final RichList<Date> dates = remaining.take(2).map(toDate).toRichList();

        if (dates.contains(null)) {
            return null;
        }

        if (fieldProcessingEnum == FieldProcessingEnum.actualDurationOvernights) {
            final RichList<Date> adjustedDates = dates.map(toEndOfDay);

            // Number of overnights
            return DateUtility.day2minusDay1(adjustedDates.get(1), adjustedDates.get(0));
        }
        
        final RichList<Integer> minutes = dates.map(toMinutes);

        final int elapsed = minutes.get(0) - minutes.get(1);

        return elapsed;
    }

    Object handleOneColumn(final Object o, final FieldProcessingEnum fieldProcessingEnum) {
        if (fieldProcessingEnum == FieldProcessingEnum.decrypt) {
            return wrappedDecryptor((String) o);
        }

        if (fieldProcessingEnum == FieldProcessingEnum.earliestBookedVisit) {
            return findExtremeVisitByStudyId((Integer) o, "min");
        }

        if (fieldProcessingEnum == FieldProcessingEnum.latestBookedVisit) {
            return findExtremeVisitByStudyId((Integer) o, "max");
        }

        if (fieldProcessingEnum == FieldProcessingEnum.SublocationFromTr) {
            return findSublocationByByTemplateResourceId((Integer) o);
        }
        if (fieldProcessingEnum == FieldProcessingEnum.ResourceFromTr) {
            return findNameById("Resource", (Integer) o);
        }
        if (fieldProcessingEnum == FieldProcessingEnum.InstitutionFromStudy) {
            return findNameById("Institution", (Integer) o);
        }
        if (fieldProcessingEnum == FieldProcessingEnum.VisitType) {
            return findNameById("VisitType", (Integer) o);
        }
        if (fieldProcessingEnum == FieldProcessingEnum.MrnFromSubjectMrn) {
            SubjectMrn subjectMrn = findById(SubjectMrn.class, (Integer) o);
            if (subjectMrn != null) {
                String decryptedMrn = wrappedDecryptor(subjectMrn.getMrn());
                String site = subjectMrn.getSite() == null ? "" : " " + subjectMrn.getSite();
                return decryptedMrn + site;
            }
            else {
                return "null";
            }
        }
        if (fieldProcessingEnum == FieldProcessingEnum.Resource) {
            return o;
        }
        if (fieldProcessingEnum == FieldProcessingEnum.DefaultPlusAnnotations) {
            return o;
        }

        if (fieldProcessingEnum != FieldProcessingEnum.DEFAULT) {
            // must match ID corresponding to one of the ref-data inner join
            // enum values!

            Object result;
            if (null == o) {
                result = "";
            }
            else {
                result = findNameById(fieldProcessingEnum.toString(), (Integer) o);
            }
            return result;
        }

        return o;
    }

    String wrappedDecryptor(String input) {

        return SubjectDataEncryptor.decrypt(input);
    }

    private static <A, B> Function<A, B> passNullsThrough(final Function<? super A, ? extends B> f) {
        return a -> (a == null) ? null : f.apply(a);
    }

    private void addToAndFrom(final Date to, final Date from, final NativeQuery query) {
        if (from != null && to != null) {
            query.setParameter("startTime", from);
            query.setParameter("endTime", to);
        }
    }

    private String makeQueryString(final Graph.QueryScalarsTcfs queryScalarsTcfs, final Date from, final Date to, final int offset, final int limit, final boolean countOnly) {
        final String qsfQuery = queryScalarsTcfs.getQueryString();

        final String countPrefix = countOnly ? "select count(*) from ( " : "";
        final String countSuffix = countOnly ? ") temp2 " : "";
        final String limits = countOnly ? "" : " limit " + limit + " offset " + offset;
        final String orderQuery = queryScalarsTcfs.getOrderString();

        final StringBuilder queryWrapper = new StringBuilder();

        queryWrapper.append(countPrefix);
        queryWrapper.append(qsfQuery);

        final List<String> lastMinuteWhereClauseList = Lists.newArrayList();
        if (from != null && to != null) {
            lastMinuteWhereClauseList.add("((:startTime between bv.scheduled_start_time and bv.scheduled_end_time) " +
                    " or (:endTime between bv.scheduled_start_time and bv.scheduled_end_time) or " +
                    " (bv.scheduled_start_time >= :startTime and bv.scheduled_end_time <= :endTime)) ");
        }

        if ( ! lastMinuteWhereClauseList.isEmpty()) {
            final String andOrWhere = (qsfQuery.toLowerCase().contains(" where ")) ? " AND " : " WHERE ";
            queryWrapper.append(andOrWhere);

            String lastMinuteWhereClausesString = Joiner.on(" AND ").join(lastMinuteWhereClauseList);
            queryWrapper.append(lastMinuteWhereClausesString);
        }

        queryWrapper.append(orderQuery);
        queryWrapper.append(limits);
        queryWrapper.append(countSuffix);

        return queryWrapper.toString();
    }

    public List<ReportTemplateMetadataDTO> findTemplateListByTypeAndUser(final User user) {
        final String query = "select t.id, t.display_name, t.type from report_template t ";

        final NativeQuery nativeQuery = newNativeQuery(query);

        @SuppressWarnings("unchecked")
        final List<Object[]> listOfResultSetObjects = nativeQuery.list();

        List<ReportTemplateMetadataDTO> finalList = enrich(listOfResultSetObjects).map(obj -> {
            final Integer id = (Integer) obj[0];
            final String name = (String) obj[1];
            final String type = (String) obj[2];
            return new ReportTemplateMetadataDTO(id, id, name, type, name, null);
        }).toList();

        finalList.addAll(findUsersReportListByTypeAndUser(user, null));
        return finalList;
    }

    public List<ReportTemplateMetadataDTO> findUsersReportListByTypeAndUser(final User user, final Integer selectedTemplate) {
        String excludeClause = " ";
        if (selectedTemplate != null) {
            excludeClause = " and tu.id != "+ selectedTemplate;
        }
        final String query = "select tu.id, tu.report_template_id, tu.name, t.type, t.display_name, tu.last_update_time " +
                             "FROM report_template t, template_user tu " +
                             "WHERE tu.report_template_id = t.id " +
                excludeClause +
                             " and tu.user_id=" + user.getId() + " ORDER BY tu.name ";

        final NativeQuery nativeQuery = newNativeQuery(query);

        @SuppressWarnings("unchecked")
        final List<Object[]> listOfResultSetObjects = nativeQuery.list();

        return enrich(listOfResultSetObjects).map(obj -> {
            final Integer id = (Integer) obj[0];
            final Integer reportTemplateId = (Integer) obj[1];
            final String name = (String) obj[2];
            final String type = (String) obj[3];
            final String baseReport = (String) obj[4];
            final Date latestUpdate = (Date) obj[5];
            return new ReportTemplateMetadataDTO(id, reportTemplateId, name, type, baseReport, latestUpdate);
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateCategoryField> findTcfsById(final List<Integer> tcfIds) {

        if(tcfIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        final String inListString = Joiner.on(",").join(tcfIds);

        final String query = "select tcf from TemplateCategoryField tcf " + "where tcf.id in (" + inListString + ") " + "order by tcf.csvOrder2, tcf.csvOrder";

        return newQuery(query).list();
    }

    @SuppressWarnings("unchecked")
    public List<TemplateUserSelection> findTemplateUserSelectionsById(final Integer templateUserId) {
        final String query = "select tus from TemplateUserSelection tus " + "where tus.templateUser.id in (" + templateUserId + ") ";

        return newQuery(query).list();
    }

    public List<TemplateUserSortSelection> findTemplateUserSortSelectionsByTemplateUserSelection(final List<Integer> templateUserSelectionId) {
        if(templateUserSelectionId.isEmpty()) {
            return new ArrayList<>();
        }

        final String inListString = Joiner.on(",").join(templateUserSelectionId);

        final String query = "select tuss from TemplateUserSortSelection tuss " + "where tuss.templateUserSelection.id in (" + inListString + ") ";

        return newQuery(query).list();
    }

    public List<TemplateUserFilterSelection> findTemplateUserFilterSelectionsByTemplateUserSelection(final List<Integer> templateUserSelectionId) {
        if(templateUserSelectionId.isEmpty()) {
            return new ArrayList<>();
        }

        final String inListString = Joiner.on(",").join(templateUserSelectionId);

        final String query = "select tufs from TemplateUserFilterSelection tufs " + "where tufs.templateUserSelection.id in (" + inListString + ") ";

        return newQuery(query).list();
    }

    Date findExtremeVisitByStudyId(final Integer study, final String minOrMax) {
        final String queryString = " select " + minOrMax + "(bv.scheduled_start_time) min from booked_visit bv where bv.study =:study";

        final NativeQuery query = newNativeQuery(queryString);

        query.setParameter("study", study);
        query.addScalar("min");

        final Date date = (Date) query.uniqueResult();

        return date;
    }

    String findSublocationByByTemplateResourceId(final Integer templateResourceId) {
        if (templateResourceId == null) {
            return null;
        }

        final String queryString = "select s.name name from sublocation s, resource r, template_resource tr, resource_sublocation rs" +
                " where tr.resource = r.id and rs.resource = r.id and rs.sublocation = s.id and tr.id =:templateResourceId";

        final NativeQuery query = newNativeQuery(queryString);

        query.setParameter("templateResourceId", templateResourceId);
        query.addScalar("name");

        final String name = (String) query.uniqueResult();

        return name;
    }

    public static String stringOrDateAsString(final Object object) {

        if (object == null || object.toString().matches("^\\s*$")) {
            return "null";
        } else if (object instanceof Date) {
            return dateToCsvFormat((Date)object);
        } else {
            return otherToCsvFormat(object);
        }
    }

    private static String otherToCsvFormat(final Object object) {
        final String result = object.toString();

        if(isLong(result)) {
            // looks like a number, so escape it for Excel
            return "\"=\"\"" + result + "\"\"\"";
        } else {
            // OK, it's not a number. escape any bad-for-csv characters
            return StringEscapeUtils.escapeCsv(result);
        }
    }

    private static String dateToCsvFormat(final Date date) {
        final String result = DateUtility.format(DateUtility.dateHourMin(), date);

        return StringEscapeUtils.escapeCsv(result);
    }
    
    private static final boolean isLong(final String stringRep) {
        return attempt(() -> Long.parseLong(stringRep)).isSuccess();
    }

    public boolean checkSavedReportName(final String reportName, final Integer reportId, final boolean isNewReport, Integer userId) {
        String sql = "select tu.name from template_user tu WHERE tu.name=\"" + reportName+"\" and tu.user_id = " + userId;
        if (!isNewReport) {
           sql += " and tu.id != "+reportId;
        }
        final NativeQuery query = newNativeQuery(sql);
        final List<Object[]> listOfResultSetObjects = query.list();
        return listOfResultSetObjects.size() != 0;
    }

    public List<Object> findListByClass(String tableName, String columnName) {
        final String findName = "SELECT table FROM " + tableName + " table ORDER BY table."+columnName;
        final Query query = newQuery(findName);
        final List<Object> result = query.list();
        return result;
    }

    public List<Object> findListByColumn(String tableName, String columnName, String filterBy, String selectedTerms) {
        String findName = "SELECT DISTINCT a."+ columnName +" FROM " + tableName + " a WHERE a."+columnName+" LIKE (\'%"+filterBy+"%\') ";

        if (!selectedTerms.isEmpty()) {
            findName += " AND a."+columnName+" NOT IN ("+selectedTerms+")";
        }
        findName += " ORDER BY a."+columnName;

        final NativeQuery query = newNativeQuery(findName);
        final List<Object> result = query.list();
        final String nullString = "Null".toLowerCase();
        if (nullString.contains(filterBy.toLowerCase())) {
            result.add("Null");
        }
        Collections.sort(result, comparatorAsc);
        return result;
    }

    public static final Comparator<Object> comparatorAsc = (o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString());
}
