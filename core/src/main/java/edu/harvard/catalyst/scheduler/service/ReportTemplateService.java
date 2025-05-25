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
package edu.harvard.catalyst.scheduler.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateCreateUsersDTO;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateRequestDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.*;
import org.apache.log4j.Logger;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.entity.HasReportFiltersNameAndId;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.Graph.QueryScalarsTcfs;
import edu.harvard.catalyst.scheduler.persistence.ReportTemplateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class ReportTemplateService {

    enum ReportOrigin {
        System {
            @Override
            Map<TemplateCategoryField, Boolean> createSelectionMap(Collection<TemplateCategoryField> tcfFullCollection,
                                                                   Collection<TemplateCategoryField> tcfSelectionCollection) {
                return createSelectionMapHelper(tcfFullCollection, false);
            }
        },
        User {
            @Override
            Map<TemplateCategoryField, Boolean> createSelectionMap(Collection<TemplateCategoryField> tcfFullCollection,
                                                                   Collection<TemplateCategoryField> tcfSelectionCollection) {
                return createSelectionMapHelper(tcfSelectionCollection, true);
            }
        };

        abstract Map<TemplateCategoryField, Boolean> createSelectionMap(Collection<TemplateCategoryField> tcfFullCollection,
                                                                        Collection<TemplateCategoryField> tcfSelectionCollection);

        Map<TemplateCategoryField, Boolean> createSelectionMapHelper(Collection<TemplateCategoryField> tcfCollection,
                                                                     boolean ignoreItsOwn) {
            Map<TemplateCategoryField, Boolean> map = new HashMap<>();

            for (TemplateCategoryField tcf : tcfCollection) {
                if (ignoreItsOwn || tcf.isSelected()) {
                    map.put(tcf, Boolean.TRUE);
                }
            }
            return map;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ReportTemplateService.class);

    public static final Integer RESULT_LIMIT = 1000;
    public static final int FIRST_LIMIT = 5;

    private final ReportTemplateDAO reportTemplateDAO;
    private final SubjectService subjectService;

    // Don't use - Needed for spring cglib proxying
    ReportTemplateService() {
        this(null, null);
    }

    @Autowired
    public ReportTemplateService(final ReportTemplateDAO reportTemplateDAO,
                                 @Qualifier("subjectSSOTConfigured") final SubjectService subjectService) {
        this.reportTemplateDAO = reportTemplateDAO;
        this.subjectService = subjectService;
    }

    public String getTemplateName(final Integer templateId, final String type, final Integer templateUserId) {
        String reportTemplateName;
        if (type.equalsIgnoreCase("Custom")) {
            reportTemplateName = reportTemplateDAO.findTemplateUserById(templateUserId).getName();
        }
        else {
            reportTemplateName = reportTemplateDAO.findTemplateById(templateId).getDisplayName();
        }
        return reportTemplateName;
    }

    QueryScalarsTcfs getQueryScalarsFields(final ReportTemplate reportTemplate, final ReportTemplateRequestDTO dto) {

        final Graph templateGraph = reportTemplate.getGraph();
        final List<Integer> templateCategoryFieldIdList = dto.getSelectedTemplateCategoryFieldIds();
        final List<TemplateCategoryField> tcfList = reportTemplateDAO.findTcfsById(templateCategoryFieldIdList);

        Map<Integer, String> tcfIdToStringSortList = dto.getTcfIdToStringSortList();
        List<Integer> tcfIdsForSortPriority = dto.getSortSelectedIds();

        Map<Integer, String> tcfIdToStringFilterList = dto.getTcfIdToStringFilterList();
        List<Integer> tcfIdsForFilters = dto.getFilterSelectedIds();

        List<TemplateCategoryField> tcfsWithFilters = reportTemplateDAO.findTcfsById(tcfIdsForFilters);
        List<TemplateCategoryField> tcfsWithSorts = reportTemplateDAO.findTcfsById(tcfIdsForSortPriority);

        return templateGraph.createQueryScalarsTcfs(tcfList, tcfIdToStringSortList, tcfIdToStringFilterList, tcfsWithFilters, tcfsWithSorts, tcfIdsForSortPriority);
    }

    public ReportTemplateResultDTO runReportTemplateOneSegment(final User user,
                                                               final String host,
                                                               final Integer templateId,
                                                               final ReportTemplateRequestDTO reportTemplateRequestDTO,
                                                               final Integer offset,
                                                               final Integer limit,
                                                               final QueryScalarsTcfs cachedQsTcfs) {

        final ReportTemplate reportTemplate = reportTemplateDAO.findTemplateById(templateId);

        final QueryScalarsTcfs queryScalarsTcfs = cachedQsTcfs == null ? getQueryScalarsFields(reportTemplate, reportTemplateRequestDTO) : cachedQsTcfs;

        final Date from = reportTemplateRequestDTO.getStartDate();
        final Date to = reportTemplateRequestDTO.getEndDate();

        final ReportTemplateResultDTO result = reportTemplateDAO.runQueryAndScalars(reportTemplate.getDisplayName(), queryScalarsTcfs,
                from, to, offset, limit);

        if (cachedQsTcfs == null) {
            result.setCachedQsTcfs(queryScalarsTcfs);
        }

        return result;
    }

    public void runReportTemplate(final User user, final String host, final Integer templateId, final ReportTemplateRequestDTO reportTemplateRequestDTO, final OutputStream output, final QueryScalarsTcfs queryScalarsTcfsParam) {

        LOGGER.info("\n--> Begin csv download");

        QueryScalarsTcfs cachedQsTcfs = queryScalarsTcfsParam;

        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(output))) {

            int currentLimit;
            int currentOffset;

            for (int i = 0; true; i++) {
                if (i == 0) {
                    currentOffset = 0;
                    currentLimit = FIRST_LIMIT;
                } else {
                    currentOffset = i * RESULT_LIMIT + FIRST_LIMIT - RESULT_LIMIT;
                    currentLimit = RESULT_LIMIT;
                }

                if (cachedQsTcfs != null) {
                    reportTemplateRequestDTO.setCachedQuery(cachedQsTcfs);
                }

                final ReportTemplateResultDTO resultsDTO =
                        runReportTemplateOneSegment(user, host, templateId, reportTemplateRequestDTO,
                                currentOffset, currentLimit, cachedQsTcfs);

                final String resultString = resultsDTO.toCsvString();

                writer.write(resultString);

                if (i == 0) {
                    // try to kick-start download
                    writer.flush();

                    // remember the hard-won QueryScalarsTcfs
                    cachedQsTcfs = resultsDTO.getCachedQsTcfs();
                }

                if (resultsDTO.getResultSize() < currentLimit) {
                    break;
                }
            }

            writer.flush();

        } catch (final IOException ioe) {
            SchedulerRuntimeException.logAndThrow("Problem running report '" + reportTemplateRequestDTO.toString() + "'");
        }

        LOGGER.info("\n--> End csv download");
    }

    public List<ReportTemplateMetadataDTO> getReportTemplateList(final User user) {
        return reportTemplateDAO.findTemplateListByTypeAndUser(user);
    }

    public List<ReportTemplateMetadataDTO> sortSavedReportTemplateList(final User user, final Integer id) {
        return reportTemplateDAO.findUsersReportListByTypeAndUser(user, id);
    }

    CategoryDTO createCategoryDTO(final ReportTemplate reportTemplate,
                                  final Category category,
                                  final boolean expanded,
                                  final ReportOrigin reportOrigin,
                                  final Set<TemplateCategoryField> userSelectedTcfs) {

        final List<TemplateCategoryField> templateCategoryFieldList = reportTemplateDAO.findTcfsByCategoryAndTemplate(category, reportTemplate);
        List<TemplateCategoryFieldDTO> tcfDTOList = createTcfDtoList(
                templateCategoryFieldList,
                reportOrigin.createSelectionMap(templateCategoryFieldList, userSelectedTcfs));

        final List<SubCategory> subCategories = reportTemplateDAO.findSubCategoriesByCategoryAndTemplate(category, reportTemplate);

        final List<CategoryDTO> childrenCategoryDTOs = Lists.newArrayList();

        for (final SubCategory subCategory : subCategories) {
            final CategoryDTO childCategoryDTO = createCategoryDTO(
                    subCategory.getReportTemplate(),
                    subCategory.getChild(),
                    subCategory.getExpanded(),
                    reportOrigin,
                    userSelectedTcfs);

            childrenCategoryDTOs.add(childCategoryDTO);
        }

        return new CategoryDTO(category.getId(), category.getDisplayName(), tcfDTOList, childrenCategoryDTOs, expanded);
    }

    public ReportTemplateDTO getReportTemplate(final Integer id) {

        final ReportTemplate reportTemplate = reportTemplateDAO.findTemplateById(id);

        List<CategoryDTO> categoryDTOList = createCategoryDtoList(reportTemplate, ReportOrigin.System, Sets.newHashSet());

        return new ReportTemplateDTO(reportTemplate.getId(), reportTemplate.getType().toString(), reportTemplate.getDisplayName(), categoryDTOList, reportTemplate.getDateBound());
    }

    List<CategoryDTO> createCategoryDtoList(ReportTemplate reportTemplate, ReportOrigin reportOrigin, Set<TemplateCategoryField> tcfUserSelections) {

        final List<TemplateCategory> templateCategories = reportTemplateDAO.findCategoriesByTemplate(reportTemplate);

        final List<CategoryDTO> categoryDTOList = Lists.newArrayList();

        for (final TemplateCategory templateCategory : templateCategories) {

            final CategoryDTO categoryDTO = createCategoryDTO(
                    templateCategory.getReportTemplate(),
                    templateCategory.getCategory(),
                    templateCategory.getExpanded(),
                    reportOrigin,
                    tcfUserSelections);

            categoryDTOList.add(categoryDTO);
        }

        return categoryDTOList;
    }

    public ReportTemplateUsersDTO getUsersReport(final Integer id) {
        final TemplateUser templateUser = reportTemplateDAO.findTemplateUserById(id);

        ReportTemplate reportTemplate = templateUser.getReportTemplate();
        Set<TemplateCategoryField> selectedTcfs = templateUser.getSelectedTcfs();
        Set<TemplateUserSelection> userSelectionList = templateUser.getUserSelections();

        List<Integer> sortList = Lists.newArrayList();
        List<String> sortDirectionList = Lists.newArrayList();

        Map<Integer, Integer> tussToTcfId = Maps.newTreeMap();
        Map<Integer, String> tussToOrderBy = Maps.newTreeMap();

        Map<Integer, Integer> tufsToTcfId = Maps.newTreeMap();
        Map<Integer, String> tufsToFilterExpression = Maps.newTreeMap();

        List<Integer> filterList = Lists.newArrayList();
        List<String> filterExpressionList = Lists.newArrayList();

        for (TemplateUserSelection tus : userSelectionList) {
            TemplateUserSortSelection tuss = tus.getUserSortSelection();
            if (tuss != null) {
                Integer tcfId = tuss.getTemplateUserSelection().getTcf().getId();
                tussToTcfId.put(tuss.getId(), tcfId);
                tussToOrderBy.put(tuss.getId(), tuss.getOrderBy());
            }

            TemplateUserFilterSelection tufs = tus.getUserFilterSelection();
            if (tufs != null) {
                Integer tcfId = tufs.getTemplateUserSelection().getTcf().getId();
                tufsToTcfId.put(tufs.getId(), tcfId);
                tufsToFilterExpression.put(tufs.getId(), tufs.getExpression());
            }
        }
        for (Integer tussId : tussToTcfId.keySet()) {
            sortList.add(tussToTcfId.get(tussId));
            sortDirectionList.add(tussToOrderBy.get(tussId));
        }

        for (Integer tufsId : tufsToTcfId.keySet()) {
            filterList.add(tufsToTcfId.get(tufsId));
            filterExpressionList.add(tufsToFilterExpression.get(tufsId));
        }

        List<CategoryDTO> categoryDTOList = createCategoryDtoList(reportTemplate, ReportOrigin.User, selectedTcfs);

        return new ReportTemplateUsersDTO(
                reportTemplate.getId(),
                reportTemplate.getType().toString(),
                reportTemplate.getDisplayName(),
                categoryDTOList,
                reportTemplate.getDateBound(),
                templateUser.getLastUpdateTime(),
                templateUser.getName(),
                sortList,
                sortDirectionList,
                filterList,
                filterExpressionList
        );
    }

    List<TemplateCategoryFieldDTO> createTcfDtoList(List<TemplateCategoryField> tcfList,
                                                    Map<TemplateCategoryField, Boolean> selectionMap) {

        final List<TemplateCategoryFieldDTO> tcfDTOList = Lists.newArrayList();

        for (final TemplateCategoryField tcf : tcfList) {
            final Field field = tcf.getField();
            final Boolean selected = (selectionMap.get(tcf) != null);
            final Boolean filterable = (field.getFieldType() != null);

            final Field.FieldType fieldType = field.getFieldType();
            final String type = fieldType != null ? fieldType.toString() : null;

            final TemplateCategoryFieldDTO tcfDTO = new TemplateCategoryFieldDTO(tcf.getId(),
                    tcf.getCategory().getDisplayName(),
                    field.getDisplayName(), selected,
                    field.isSortable(), filterable,
                    type);

            tcfDTOList.add(tcfDTO);
        }

        Collections.sort(tcfDTOList, new TemplateCategoryFieldDTO.TemplateCategoryFieldsComparatorAsc());

        return tcfDTOList;
    }

    public ReportTemplateMetadataDTO createUserReport(User user, ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO, Integer id) {
        ReportTemplate reportTemplate = reportTemplateDAO.findTemplateById(id);
        ReportTemplateMetadataDTO reportTemplateMetadataDTO = null;
        String name = reportTemplateCreateUsersDTO.getReportName();
        boolean exists = checkSavedReportNameExists(name, null, true, user);
        if (!exists) {
            TemplateUser templateUser = new TemplateUser(reportTemplate, user, name);
            reportTemplateDAO.createEntity(templateUser);
            createTcfsForSavedReport(reportTemplateCreateUsersDTO, templateUser);
            reportTemplateMetadataDTO = new ReportTemplateMetadataDTO(templateUser.getId(),
                    templateUser.getReportTemplate().getId(), templateUser.getName(), "Administrative",
                    templateUser.getReportTemplate().getDisplayName(), templateUser.getLastUpdateTime());
        }
        return reportTemplateMetadataDTO;
    }

    private boolean checkSavedReportNameExists(String name, Integer id, boolean isNewReport, User user) {
        return reportTemplateDAO.checkSavedReportName(name, id, isNewReport, user.getId());
    }

    public ReportTemplateMetadataDTO updateUserReport(User user, ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO, Integer id) {
        TemplateUser templateUser = reportTemplateDAO.findTemplateUserById(id);
        ReportTemplateMetadataDTO reportTemplateMetadataDTO = null;
        String name = reportTemplateCreateUsersDTO.getReportName();
        boolean exists = checkSavedReportNameExists(name, templateUser.getId(), false, user);
        if (!exists) {
            templateUser.setLastUpdateTime(Calendar.getInstance().getTime());
            templateUser.setName(name);
            templateUser.setUser(user);
            reportTemplateDAO.updateEntity(templateUser);

            List<TemplateUserSelection> templateUserSelectionList = reportTemplateDAO.findTemplateUserSelectionsById(templateUser.getId());

            deleteUserSortSelections(templateUserSelectionList);
            deleteUserFilterSelections(templateUserSelectionList);

            templateUserSelectionList.forEach(reportTemplateDAO::deleteEntity);

            createTcfsForSavedReport(reportTemplateCreateUsersDTO, templateUser);
            reportTemplateMetadataDTO = new ReportTemplateMetadataDTO(templateUser.getId(),
                    templateUser.getReportTemplate().getId(), templateUser.getName(), "Administrative",
                    templateUser.getReportTemplate().getDisplayName(), templateUser.getLastUpdateTime());
        }
        return reportTemplateMetadataDTO;
    }

    void deleteUserSortSelections(List<TemplateUserSelection> templateUserSelectionList) {

        List<Integer> selectionIds = templateUserSelectionList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<TemplateUserSortSelection> templateUserSortSelectionList = reportTemplateDAO.findTemplateUserSortSelectionsByTemplateUserSelection(selectionIds);
        templateUserSortSelectionList.forEach(reportTemplateDAO::deleteEntity);
    }

    void deleteUserFilterSelections(List<TemplateUserSelection> templateUserSelectionList) {

        List<Integer> selectionIds = templateUserSelectionList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<TemplateUserFilterSelection> templateUserFilterSelectionList = reportTemplateDAO.findTemplateUserFilterSelectionsByTemplateUserSelection(selectionIds);
        templateUserFilterSelectionList.forEach(reportTemplateDAO::deleteEntity);
    }

    private void createTcfsForSavedReport(ReportTemplateCreateUsersDTO dto, TemplateUser templateUser) {
        Set<TemplateCategoryField> selectedTcfsSet =
                Sets.newHashSet(reportTemplateDAO.findTcfsById(dto.getSelectedTemplateCategoryFieldIds()));

        Map<Integer, String> tcfIdToStringSortList = dto.getTcfIdToStringSortList();
        List<Integer> idsOfTcfsWithSorts = dto.getSortSelectedIds();

        Map<Integer, String> tcfIdToStringFilterList = dto.getTcfIdToStringFilterList();
        List<Integer> idsOfTcfsWithFilters = dto.getFilterSelectedIds();

        List<TemplateCategoryField> tcfsWithFilters = reportTemplateDAO.findTcfsById(idsOfTcfsWithFilters);
        List<TemplateCategoryField> tcfsWithSorts = reportTemplateDAO.findTcfsById(idsOfTcfsWithSorts);

        Map<Integer, TemplateUserFilterSelection> tcfToNewFilterSelection = Maps.newHashMap();
        Map<Integer, TemplateUserSortSelection> tcfToNewSortSelection = Maps.newHashMap();

        for (TemplateCategoryField selectedTcf : selectedTcfsSet) {
            TemplateUserSelection templateUserSelection = new TemplateUserSelection(templateUser, selectedTcf);
            reportTemplateDAO.createEntity(templateUserSelection);

            if (tcfsWithSorts.contains(selectedTcf)) {
                String orderBy = tcfIdToStringSortList.get(selectedTcf.getId());
                TemplateUserSortSelection sortSelection = new TemplateUserSortSelection(templateUserSelection, orderBy);
                tcfToNewSortSelection.put(selectedTcf.getId(), sortSelection);
            }

            if (tcfsWithFilters.contains(selectedTcf)) {
                String expression = tcfIdToStringFilterList.get(selectedTcf.getId());
                Field tcfField = selectedTcf.getField();
                Field.FieldType fieldType = tcfField.getFieldType();

                if (fieldType == null) {
                    SchedulerRuntimeException.logAndThrow("Hey, field " + tcfField.getDisplayName() + " doesn't have a filter!");
                }

                TemplateUserFilterSelection filterSelection = new TemplateUserFilterSelection(templateUserSelection, expression);
                tcfToNewFilterSelection.put(selectedTcf.getId(), filterSelection);
            }
        }
        for (Integer tcfId : idsOfTcfsWithFilters) {
            TemplateUserFilterSelection templateUserFilterSelection = tcfToNewFilterSelection.get(tcfId);
            reportTemplateDAO.createEntity(templateUserFilterSelection);
        }
        for (Integer tcfId : idsOfTcfsWithSorts) {
            TemplateUserSortSelection templateUserSortSelection = tcfToNewSortSelection.get(tcfId);
            reportTemplateDAO.createEntity(templateUserSortSelection);
        }
    }

    public BooleanResultDTO deleteUsersReport(Integer id) {
        TemplateUser templateUser = reportTemplateDAO.findTemplateUserById(id);
        List<TemplateUserSelection> templateUserSelectionList = reportTemplateDAO.findTemplateUserSelectionsById(templateUser.getId());

        deleteUserSortSelections(templateUserSelectionList);
        deleteUserFilterSelections(templateUserSelectionList);

        templateUserSelectionList.forEach(reportTemplateDAO::deleteEntity);
        reportTemplateDAO.deleteEntity(templateUser);
        return new BooleanResultDTO(true);
    }

    public List<Object> getStaticListByTcf(Integer tcfId, String columnName) {
        TemplateCategoryField tcf = reportTemplateDAO.findById(TemplateCategoryField.class, tcfId);
        return reportTemplateDAO.findListByClass(tcf.getField().getFieldProcessingEnum().getStaticClassName(), columnName);
    }

    public List<Field.HasReportFiltersNameAndIdPair> getEnumListByTcf(Integer tcfId) {
        TemplateCategoryField tcf = reportTemplateDAO.findById(TemplateCategoryField.class, tcfId);
        Field enumTypeField = tcf.getField();

        List<HasReportFiltersNameAndId> enumList = Field.FieldType.getFieldTypeEnums(enumTypeField);
        enumList.sort((p1, p2) -> p1.getReportFiltersName().compareToIgnoreCase(p2.getReportFiltersName()));
        return enumList.stream().map(Field.HasReportFiltersNameAndIdPair::new).collect(Collectors.toList());
    }

    public List<Object> getListsByField(Integer tcfId, String filterBy, String selectedTerms) {
        final TemplateCategoryField tcf = reportTemplateDAO.findById(TemplateCategoryField.class, tcfId);
        final String tableName = tcf.getField().getTable();
        final String columnName = tcf.getField().getColumn();
        if (!selectedTerms.isEmpty()) {
            selectedTerms = Field.FieldType.quotifyCommaString(selectedTerms, "'", "'", ",");
        }
        return reportTemplateDAO.findListByColumn(tableName, columnName, filterBy, selectedTerms);
    }
}
