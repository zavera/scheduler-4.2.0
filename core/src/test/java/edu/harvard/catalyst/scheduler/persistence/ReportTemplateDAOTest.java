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

import com.google.common.collect.Lists;
import edu.harvard.catalyst.hccrc.core.util.RichIterator;
import edu.harvard.catalyst.scheduler.AbstractSpringWiredHibernateTest;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.*;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateMetadataDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateResultDTO;
import edu.harvard.catalyst.scheduler.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class ReportTemplateDAOTest extends AbstractSpringWiredHibernateTest {

    @Autowired
    private ReportTemplateDAO dao;

    @Test
    public void testRunQueryAndScalars() throws Exception {

        // category 109 maps to bookedResource type of graphOne
        final Category category = new Category(109, "cat", "My Category", "My Cat");
        final ReportTemplate reportTemplate = new ReportTemplate();
        Map<Integer, String> tcfIdToStringSortList = null;
        Map<Integer, String> tcfIdToStringFilterList = null;
        final List<TemplateCategoryField> fieldList = Lists.newArrayList(
                new TemplateCategoryField(1, reportTemplate, category, new Field(1, "booked_visit", "id", "ID-Name"), "", true));

        final BookedVisitGraph theGraph = BookedVisitGraph.getInstance();

        final Graph.QueryScalarsTcfs queryScalarsTcfs = theGraph.createQueryScalarsTcfs(fieldList,
                                                                tcfIdToStringSortList, tcfIdToStringFilterList,
                                                                Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

        final ReportTemplateResultDTO result = dao.runQueryAndScalars("blah report", queryScalarsTcfs,
                null, null, 0, 0);

        assert result != null;

        assertEquals("blah report", result.getReportName());

        TestUtils.assertNonNullAndHasThisMany(result.getCsvHeaders(), 1);

        assertEquals("My Cat - ID-Name", result.getCsvHeaders().get(0));
        TestUtils.assertNonNullAndHasThisMany(result.getCsvRows(), 0);

        return;
    }

    @Test
    public void testFindTemplateById() throws Exception {
        final ReportTemplate result = dao.findTemplateById(42);

        // should be empty DB unless we programmatically populateMany
        // but query should work
        assertNull(result);
    }
    @Test
    public void findTcfsById() throws Exception {
        final List<TemplateCategoryField> result = dao.findTcfsById(Lists.newArrayList(42));

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }
    @Test
    public void findTemplateListByTypeAndUser() throws Exception {
        final List<ReportTemplateMetadataDTO> result = dao.findTemplateListByTypeAndUser(new User());

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }
    @Test
    public void testFindCategoriesByTemplate() throws Exception {
        final ReportTemplate reportTemplate = new ReportTemplate();
        this.save(reportTemplate);

        final List<TemplateCategory> result = dao.findCategoriesByTemplate(reportTemplate);

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }
    @Test
    public void testFindSubCategoriesByCategoryAndTemplate() throws Exception {
        final ReportTemplate reportTemplate = new ReportTemplate();
        final Category category = new Category();
        this.save(reportTemplate);
        this.save(category);

        final List<SubCategory> result = dao.findSubCategoriesByCategoryAndTemplate(category, reportTemplate);

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }
    @Test
    public void testFindTcfsByCategoryAndTemplate() throws Exception {
        final ReportTemplate reportTemplate = new ReportTemplate();
        final Category category = new Category();
        this.save(reportTemplate);
        this.save(category);

        final List<TemplateCategoryField> result = dao.findTcfsByCategoryAndTemplate(category, reportTemplate);

        TestUtils.assertNonNullAndHasThisMany(result, 0);
    }
    @Test
    public void testStringOrDateAsString() throws Exception {
        String result;

        result = ReportTemplateDAO.stringOrDateAsString(null);
        assertEquals("null", result);

        result = ReportTemplateDAO.stringOrDateAsString("joey");
        assertEquals("joey", result);

        result = ReportTemplateDAO.stringOrDateAsString(new Integer(1));
        assertEquals("\"=\"\"1\"\"\"", result);

        result = ReportTemplateDAO.stringOrDateAsString(Boolean.FALSE);
        assertEquals("false", result);

        result = ReportTemplateDAO.stringOrDateAsString(new Date());
        assert result.matches(
                "\\d{4}-\\d{2}-\\d{2} " +
                "\\d{2}:\\d{2}");

        result = ReportTemplateDAO.stringOrDateAsString(new Object() {
            @Override
            public String toString() {
                return "just for fun";
            }
        });
        assertEquals("just for fun", result);
    }


    @Test
    public void testToCsvString() throws Exception {
        helpTestToCsvStringOvernights(0, "0");
        helpTestToCsvStringOvernights(1, "1");
        
        helpTestToCsvStringOther(0, "0");
        helpTestToCsvStringOther(42, "42");
    }

    public void helpTestToCsvStringOvernights(final int daysToAdd, final String expected) throws Exception {
        final Field field = new Field(null, null, "date1,date2", null);
        
        field.setFieldProcessingEnum(FieldProcessingEnum.actualDurationOvernights);

        final Date baseDate = new Date();
        
        final Date laterDate = adjustHMS(baseDate, daysToAdd, 14, 1, 1);
        final Date earlierDate = adjustHMS(baseDate, 0, 11, 1, 1);

        final Object[] row = { laterDate, earlierDate };

        final RichIterator<Object> remaining = enrich(row).richIterator();
        
        final String result = dao.toCsvField(remaining).apply(field);
        
        //We expect our row iterator to be fully consumed
        assertEquals(Collections.emptyList(), remaining.toList());
        assertEquals(expected, declutterCsvNumber(result));

        final Object[] rowNulls = { null, null };
        final RichIterator<Object> remaining2 = enrich(rowNulls).richIterator();

        final String result2 = dao.toCsvField(remaining2).apply(field);

        //We expect our row iterator to be fully consumed
        assertEquals(Collections.emptyList(), remaining2.toList());
    }
    
    public void helpTestToCsvStringOther(final int minutesToAdd, final String expected) throws Exception {
        final Field field = new Field(null, null, "date1,date2", null);
        
        //NB: leave FieldProcessingEnum as 'default' 

        final Date laterDate = adjustHMS(new Date(), 0, 0, minutesToAdd, 0);
        final Date earlierDate = adjustHMS(new Date(), 0, 0, 0, 0);

        final Object[] row = { laterDate, earlierDate };

        final RichIterator<Object> remaining = RichIterator.enrich(enrich(row).iterator());
        
        final String result = dao.toCsvField(remaining).apply(field);
        
        //We expect our row iterator to be fully consumed
        assertEquals(Collections.emptyList(), remaining.toList());
        
        assertEquals(expected, declutterCsvNumber(result));
    }

    private Date adjustHMS(final Date date, final int days, final int hour, final int minute, final int second) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();

    }
    private String declutterCsvNumber(final String clutteredNumber) {
        return clutteredNumber.replaceAll("[\"=]", "");
    }

    @Test
    public void testHandleFourColumns() {
        LocalDateTime ldt10s = LocalDateTime.of(2015, 9, 9, 10, 10);
        LocalDateTime ldt10s12 = LocalDateTime.of(2015, 9, 9, 10, 12);
        LocalDateTime ldt10s14 = LocalDateTime.of(2015, 9, 9, 10, 14);
        LocalDateTime ldt10s17 = LocalDateTime.of(2015, 9, 9, 10, 17);

        RichIterator<Object> riOneNull = enrich(Lists.newArrayList(ldt10s, null).stream()
                .map(ldt -> (Object)(DateUtility.toDate(ldt)))
                .collect(Collectors.toList()))
                .richIterator();

        RichIterator<Object> riOneMinute = enrich(Lists.newArrayList(ldt10s17, ldt10s14, ldt10s12, ldt10s).stream()
                .map(ldt -> (Object)(DateUtility.toDate(ldt)))
                .collect(Collectors.toList()))
                .richIterator();

        Integer result = dao.handleFourColumns(riOneNull);
        assertNull(result);
    }
    @Test
    public void testHandleTwoColumns() {
        LocalDateTime ldt10s = LocalDateTime.of(2015, 9, 9, 10, 10);
        LocalDateTime ldt10s12 = LocalDateTime.of(2015, 9, 9, 10, 12);
        LocalDateTime ldt10s11s = LocalDateTime.of(2015, 9, 10, 10, 11);

        RichIterator<Object> riOneNull = enrich(Lists.newArrayList(ldt10s, null).stream()
                .map(ldt -> (Object)(DateUtility.toDate(ldt)))
                .collect(Collectors.toList()))
                .richIterator();

        RichIterator<Object> ri2minutes = enrich(Lists.newArrayList(ldt10s12, ldt10s).stream()
                .map(ldt -> (Object)(DateUtility.toDate(ldt)))
                .collect(Collectors.toList()))
                .richIterator();

        RichIterator<Object> ri1day = enrich(Lists.newArrayList(ldt10s11s, ldt10s).stream()
                .map(ldt -> (Object)(DateUtility.toDate(ldt)))
                .collect(Collectors.toList()))
                .richIterator();

        Integer result = dao.handleTwoColumns(riOneNull, FieldProcessingEnum.actualDuration);
        assertNull(result);

        result = dao.handleTwoColumns(ri2minutes, FieldProcessingEnum.actualDuration);
        assertEquals(Integer.valueOf(2), result);

        result = dao.handleTwoColumns(ri1day, FieldProcessingEnum.actualDurationOvernights);
        assertEquals(Integer.valueOf(1), result);
    }
    @Test
    public void testHandleOneColumn() {
        SubjectMrn subjectMrn = new SubjectMrn();
        ReportTemplateDAO daoSpy = spy(new ReportTemplateDAO());

        doReturn(null).when(daoSpy).wrappedDecryptor(null);
        doReturn(null).when(daoSpy).findExtremeVisitByStudyId(null, "min");
        doReturn(null).when(daoSpy).findExtremeVisitByStudyId(null, "max");
        doReturn(null).when(daoSpy).findSublocationByByTemplateResourceId(null);
        doReturn(null).when(daoSpy).findNameById("Resource", null);
        doReturn(null).when(daoSpy).findNameById("Institution", null);
        doReturn(null).when(daoSpy).findNameById("VisitType", null);
        doReturn(null).when(daoSpy).findById(SubjectMrn.class, null);

        daoSpy.handleOneColumn(null, FieldProcessingEnum.decrypt);
        verify(daoSpy).wrappedDecryptor(null);

        daoSpy.handleOneColumn(null, FieldProcessingEnum.earliestBookedVisit );
        verify(daoSpy).findExtremeVisitByStudyId(null, "min");

        daoSpy.handleOneColumn(null, FieldProcessingEnum.latestBookedVisit );
        verify(daoSpy).findExtremeVisitByStudyId(null, "max");

        daoSpy.handleOneColumn(null, FieldProcessingEnum.SublocationFromTr );
        verify(daoSpy).findSublocationByByTemplateResourceId(null);

        daoSpy.handleOneColumn(null, FieldProcessingEnum.ResourceFromTr );
        verify(daoSpy).findNameById("Resource", null);

        daoSpy.handleOneColumn(null, FieldProcessingEnum.InstitutionFromStudy );
        verify(daoSpy).findNameById("Institution", null);

        Object result = daoSpy.handleOneColumn(null, FieldProcessingEnum.MrnFromSubjectMrn );
        verify(daoSpy).findById(SubjectMrn.class, null);
        assertEquals("null", result);

        // second invocation of some of these mocked methods
        doReturn(subjectMrn).when(daoSpy).findById(SubjectMrn.class, null);
        daoSpy.handleOneColumn(null, FieldProcessingEnum.MrnFromSubjectMrn );
        verify(daoSpy, times(2)).findById(SubjectMrn.class, null);
        verify(daoSpy, times(2)).wrappedDecryptor(null);

        result = daoSpy.handleOneColumn(null, FieldProcessingEnum.Resource );
        assertNull(result);

        result = daoSpy.handleOneColumn(null, FieldProcessingEnum.DefaultPlusAnnotations );
        assertNull(result);

        result = daoSpy.handleOneColumn(null, FieldProcessingEnum.DEFAULT );
        assertNull(result);

        result = daoSpy.handleOneColumn(null, FieldProcessingEnum.VisitType );
        assertNull(result);

        // other, non default
        result = daoSpy.handleOneColumn(null, FieldProcessingEnum.actualDuration );
        assertEquals("", result);

        doReturn("blah").when(daoSpy).findNameById("actualDuration", 1);
        result = daoSpy.handleOneColumn(1, FieldProcessingEnum.actualDuration );
        assertEquals("blah", result);
    }
}
