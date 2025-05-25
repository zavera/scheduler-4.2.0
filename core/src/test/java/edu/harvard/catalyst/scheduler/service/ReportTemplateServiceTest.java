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
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.BooleanResultDTO;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateCreateUsersDTO;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateRequestDTO;
import edu.harvard.catalyst.scheduler.dto.response.*;
import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.*;
import edu.harvard.catalyst.scheduler.util.ServiceTestsDAOUtility;
import edu.harvard.catalyst.scheduler.util.TestUtils;
import edu.harvard.catalyst.scheduler.persistence.ReportTemplateDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/13/14
 * Time: 10:39 AM
 */

@RunWith(MockitoJUnitRunner.class)
public final class ReportTemplateServiceTest {

    @Mock private ReportTemplateDAO mockReportTemplateDAO;
    @Mock private ReportTemplate mockReportTemplate;
    @Mock private EpicSubjectService mockEpicSubjectService;
    @Mock private TemplateUser templateUser;
    @Mock private ReportTemplateCreateUsersDTO mockReportTCUDTO;

    private ReportTemplateService reportTemplateService;

    private List<TemplateCategoryField> tcfList;
    private List<SubCategory> subCategoryList;
    private Field field;
    private Category category;
    private Category category2;
    private ReportTemplate reportTemplate;
    private SubCategory subCategory;

    final private int templateIdMagicNumber = 42;
    final private int tcfIdMagicNumber = 43;
    final private int templateUserId = 10;

    final private Date lastUpdate = Calendar.getInstance().getTime();

    @Before
    public void setup() {

        reportTemplateService = new ReportTemplateService(mockReportTemplateDAO, mockEpicSubjectService);

        this.field = new Field(11, "table", "column", "fieldDisplay");
        field.setFieldType(Field.FieldType.ShortString);
        this.category = new Category(101, "name", "categoryDisplay", "C1");
        this.category2 = new Category(102, "name2", "categoryDisplay2", "C2");
        this.reportTemplate = new ReportTemplate(
                templateIdMagicNumber,
                "reportDisplay",
                ReportTemplate.TemplateType.Administrative,
                true,
                CommonGraph.GraphType.BookedVisit);
        this.tcfList = Lists.newArrayList();
        final TemplateCategoryField tcf = new TemplateCategoryField(tcfIdMagicNumber, reportTemplate, category, field, "", true);
        this.tcfList.add(tcf);

        this.subCategoryList = Lists.newArrayList();
        this.subCategory = new SubCategory(1, reportTemplate, category, category2, true);
        this.subCategoryList.add(subCategory);
        final TemplateCategory templateCategory = new TemplateCategory(301, reportTemplate, category, true);
        final List<TemplateCategory> templateCategories = Lists.newArrayList(templateCategory);

        Mockito.when(mockReportTemplateDAO.findTemplateById(templateIdMagicNumber)).thenReturn(reportTemplate);
        Mockito.when(mockReportTemplateDAO.findCategoriesByTemplate(reportTemplate)).thenReturn(templateCategories);
        Mockito.when(mockReportTemplateDAO.findTcfsByCategoryAndTemplate(category, reportTemplate)).thenReturn(tcfList);
        Mockito.when(mockReportTemplateDAO.findSubCategoriesByCategoryAndTemplate(category, reportTemplate))
                .thenReturn(subCategoryList);
        Mockito.when(mockReportTemplateDAO.findById(TemplateCategoryField.class, tcfIdMagicNumber)).thenReturn(this.tcfList.get(0));
        Mockito.when(mockReportTemplateDAO.findTemplateUserById(templateUserId)).thenReturn(templateUser);
        Mockito.when(templateUser.getReportTemplate()).thenReturn(reportTemplate);
        Mockito.when(templateUser.getSelectedTcfs()).thenReturn(Sets.newHashSet());
        Mockito.when(templateUser.getName()).thenReturn("joey");
        Mockito.when(templateUser.getId()).thenReturn(templateUserId);
        Mockito.when(templateUser.getLastUpdateTime()).thenReturn(lastUpdate);
    }

    @Test
    public void testGetTemplateName() {
        Mockito.when(mockReportTemplateDAO.findTemplateById(1)).thenReturn(mockReportTemplate);
        Mockito.when(mockReportTemplate.getDisplayName()).thenReturn("yogi");

        final String result = reportTemplateService.getTemplateName(1, "Administrative", 1);
        Assert.assertEquals("yogi", result);

        Mockito.verify(mockReportTemplateDAO).findTemplateById(1);
    }

    @Test
    public void testRunReportTemplate() {
        final Category category = new Category(109, "cat", "category", "cat");
        final List<TemplateCategoryField> fieldList = Lists.newArrayList(
                new TemplateCategoryField(1, null, category, new Field(1, "line_level_annotations", "name", "Annot-Name"), "_br", true));
        final List<Integer> fieldIdList = Lists.newArrayList(1);
        final Map<Integer, String> tcfIdToStringSortList = new HashMap<>();
        final Map<Integer, String> tcfIdToStringFilterList = new HashMap<>();

        final ReportTemplateRequestDTO reportTemplateRequestDTO = new ReportTemplateRequestDTO();
        reportTemplateRequestDTO.setSelectedTemplateCategoryFieldIds(fieldIdList);
        reportTemplateRequestDTO.setFilterSelectedIds(Lists.newArrayList());
        reportTemplateRequestDTO.setSortSelectedIds(Lists.newArrayList());

        final ReportTemplateResultDTO mockReportTemplateResultDTO = Mockito.mock(ReportTemplateResultDTO.class);

        final BookedVisitGraph theGraph = BookedVisitGraph.getInstance();

        final Graph.QueryScalarsTcfs queryScalarsTcfs = theGraph.createQueryScalarsTcfs(fieldList,
                                             tcfIdToStringSortList, tcfIdToStringFilterList,
                                             Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList());

        Mockito.when(mockReportTemplate.getGraph()).thenReturn(theGraph);
        Mockito.when(mockReportTemplate.getDisplayName()).thenReturn("myName");

        Mockito.when(mockReportTemplateDAO.findTemplateById(1)).thenReturn(mockReportTemplate);
        Mockito.when(mockReportTemplateDAO.findTcfsById(fieldIdList)).thenReturn(fieldList);
        reportTemplateRequestDTO.setTcfIdToStringSortList(new HashMap<>());
        reportTemplateRequestDTO.setTcfIdToStringFilterList(new HashMap<>());

        Mockito.when(mockReportTemplateDAO.runQueryAndScalars("myName", queryScalarsTcfs,
                null, null, 0, 0)).thenReturn(mockReportTemplateResultDTO);

        reportTemplateService.runReportTemplateOneSegment(null, null, 1, reportTemplateRequestDTO, 0, 0, null);

        Mockito.verify(mockReportTemplateDAO).findTemplateById(1);
        Mockito.verify(mockReportTemplateDAO).runQueryAndScalars("myName", queryScalarsTcfs, null, null, 0, 0);
    }

    @Test
    public void testGetReportTemplateList() {
        Mockito.when(mockReportTemplateDAO.findTemplateById(1)).thenReturn(mockReportTemplate);

        final User user = ServiceTestsDAOUtility.createMockUser();

        final List<ReportTemplateMetadataDTO> result = reportTemplateService.getReportTemplateList(user);
        TestUtils.assertNonNullAndHasThisMany(result, 0);

        Mockito.verify(mockReportTemplateDAO).findTemplateListByTypeAndUser(user);
    }

    @Test
    public void testSortSavedReportTemplateList() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final List<ReportTemplateMetadataDTO> list = new ArrayList<>();
        Mockito.when(mockReportTemplateDAO.findUsersReportListByTypeAndUser(user, 20)).thenReturn(list);

        List<ReportTemplateMetadataDTO> results = reportTemplateService.sortSavedReportTemplateList(user, 20);

        Assert.assertNotNull(results);
        Assert.assertTrue(results == list);
        Mockito.verify(mockReportTemplateDAO).findUsersReportListByTypeAndUser(user, 20);
    }

    @Test
    public void testCreateCategoryDTO() {

        final CategoryDTO result = reportTemplateService.createCategoryDTO(reportTemplate, category,
                true, ReportTemplateService.ReportOrigin.System, Sets.newHashSet());

        this.checkCategoryDTO(result);
    }

    @Test
    public void getReportTemplate() {

        final ReportTemplateDTO result = reportTemplateService.getReportTemplate(templateIdMagicNumber);

        final List<CategoryDTO> categoryDTOs = result.getCategories();
        TestUtils.assertNonNullAndHasThisMany(categoryDTOs, 1);

        final CategoryDTO resultCategory = categoryDTOs.get(0);
        checkCategoryDTO(resultCategory);

        Assert.assertTrue(result.getDateBounded());

        Assert.assertEquals("reportDisplay", result.getDisplayName());
        Assert.assertEquals(new Integer(templateIdMagicNumber), result.getId());
        Assert.assertEquals(ReportTemplate.TemplateType.Administrative.toString(), result.getType());
    }

    @Test
    public void getUsersReport() {
        final ReportTemplateUsersDTO result = reportTemplateService.getUsersReport(10);

        Assert.assertEquals("joey", result.getName());
        Assert.assertEquals(lastUpdate, result.getLatestUpdate());
    }

    private void checkCategoryDTO(CategoryDTO result) {
        Assert.assertEquals(new Integer(101), result.getId());
        Assert.assertEquals("categoryDisplay", result.getDisplayName());
        Assert.assertTrue(result.getExpanded());

        final List<TemplateCategoryFieldDTO> tcfDTOList = result.getTemplateCategoryFieldList();
        TestUtils.assertNonNullAndHasThisMany(tcfDTOList, 1);

        final List<CategoryDTO> subCategories = result.getSubCategories();
        TestUtils.assertNonNullAndHasThisMany(subCategories, 1);

        TemplateCategoryFieldDTO tcfDTO = tcfDTOList.get(0);
        Assert.assertEquals(new Integer(tcfIdMagicNumber), tcfDTO.getId());
        Assert.assertEquals("fieldDisplay", tcfDTO.getDisplayName());
        Assert.assertTrue(tcfDTO.getSelected());

        final CategoryDTO category2DTO = subCategories.get(0);
        Assert.assertEquals(new Integer(102), category2DTO.getId());
        Assert.assertEquals("categoryDisplay2", category2DTO.getDisplayName());
        Assert.assertTrue(category2DTO.getExpanded());
        TestUtils.assertNonNullAndHasThisMany(category2DTO.getSubCategories(), 0);
        TestUtils.assertNonNullAndHasThisMany(category2DTO.getTemplateCategoryFieldList(), 0);
    }

    @Test
    public void testCreateUsersReport() {
        final ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO = new ReportTemplateCreateUsersDTO();
        final List<Integer> selectedTcfIds = Lists.newArrayList(tcfIdMagicNumber);
        final Map<Integer, String> tcfIdToStringList = Maps.newHashMap();
        final User user = ServiceTestsDAOUtility.createMockUser();
        reportTemplateCreateUsersDTO.setReportName("joey");
        reportTemplateCreateUsersDTO.setSelectedTemplateCategoryFieldIds(selectedTcfIds);
        reportTemplateCreateUsersDTO.setTcfIdToStringFilterList(tcfIdToStringList);
        reportTemplateCreateUsersDTO.setTcfIdToStringSortList(tcfIdToStringList);
        reportTemplateCreateUsersDTO.setFilterSelectedIds(Lists.newArrayList());
        reportTemplateCreateUsersDTO.setSortSelectedIds(Lists.newArrayList());

        Mockito.when(mockReportTemplateDAO.checkSavedReportName("joey", null, true, 1)).thenReturn(false);
        Mockito.when(mockReportTemplateDAO.findTcfsById(reportTemplateCreateUsersDTO.getSelectedTemplateCategoryFieldIds())).thenReturn(tcfList);

        reportTemplateService.createUserReport(user, reportTemplateCreateUsersDTO, templateIdMagicNumber);

        Mockito.verify(mockReportTemplateDAO).findTemplateById(templateIdMagicNumber);
        Mockito.verify(mockReportTemplateDAO).checkSavedReportName("joey", null, true, 1);
        Mockito.verify(mockReportTemplateDAO).findTcfsById(selectedTcfIds);
        Mockito.verify(mockReportTemplateDAO, Mockito.times(2)).createEntity(Mockito.any(TemplateUserSelection.class));
    }

    @Test
    public void testCreateUserAlreadyExists() {
        final ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO = new ReportTemplateCreateUsersDTO();
        final List<Integer> selectedTcfIds = Lists.newArrayList(tcfIdMagicNumber);
        final Map<Integer, String> tcfIdToStringList = Maps.newHashMap();
        final User user = ServiceTestsDAOUtility.createMockUser();
        reportTemplateCreateUsersDTO.setReportName("joey");
        reportTemplateCreateUsersDTO.setSelectedTemplateCategoryFieldIds(selectedTcfIds);
        reportTemplateCreateUsersDTO.setTcfIdToStringFilterList(tcfIdToStringList);
        reportTemplateCreateUsersDTO.setTcfIdToStringSortList(tcfIdToStringList);
        reportTemplateCreateUsersDTO.setFilterSelectedIds(Lists.newArrayList());
        reportTemplateCreateUsersDTO.setSortSelectedIds(Lists.newArrayList());

        Mockito.when(mockReportTemplateDAO.checkSavedReportName("joey", null, true, user.getId())).thenReturn(true);
        Mockito.when(mockReportTemplateDAO.findTcfsById(reportTemplateCreateUsersDTO.getSelectedTemplateCategoryFieldIds())).thenReturn(tcfList);

        final ReportTemplateMetadataDTO result = reportTemplateService.createUserReport(user,
                reportTemplateCreateUsersDTO, templateIdMagicNumber);

        Assert.assertNull(result);
        Mockito.verify(mockReportTemplateDAO).findTemplateById(templateIdMagicNumber);
        Mockito.verify(mockReportTemplateDAO).checkSavedReportName("joey", null, true, 1);
    }

    @Test(expected=SchedulerRuntimeException.class)
    public void testUpdateUserReportExceptionThrown() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final TemplateUser localTemplateUser = new TemplateUser();
        localTemplateUser.setId(111);
        localTemplateUser.setReportTemplate(reportTemplate);

        final List<Integer> selectedTemplateIdList = new ArrayList<>();
        selectedTemplateIdList.add(323);

        final List<TemplateCategoryField> filterFieldList = new ArrayList<>();
        final TemplateCategoryField filterTCF = new TemplateCategoryField(67, new ReportTemplate(),
                new Category(), new Field(), "", false);
        filterFieldList.add(filterTCF);
        filterFieldList.add(tcfList.get(0));

        Mockito.when(mockReportTCUDTO.getSelectedTemplateCategoryFieldIds()).thenReturn(selectedTemplateIdList);
        Mockito.when(mockReportTemplateDAO.findTcfsById(Mockito.anyListOf(Integer.class))).thenReturn(filterFieldList);
        Mockito.when(mockReportTemplateDAO.findTemplateUserById(localTemplateUser.getId())).thenReturn(localTemplateUser);


        reportTemplateService.updateUserReport(
                user,
                mockReportTCUDTO,
                111);
    }

    @Test
    public void testUpdateUserReport() {
        final User user = ServiceTestsDAOUtility.createMockUser();
        final TemplateUser localTemplateUser = new TemplateUser();
        localTemplateUser.setId(111);
        localTemplateUser.setReportTemplate(reportTemplate);

        final Map<Integer, String> sortMap = new HashMap<>();
        final Map<Integer, String> filterMap = new HashMap<>();
        final List<Integer> sortList = new ArrayList<>();
        sortList.add(123);
        final List<Integer> filterList = new ArrayList<>();
        filterList.add(223);

        final List<Integer> selectedTemplateIdList = new ArrayList<>();
        selectedTemplateIdList.add(323);

        final List<TemplateCategoryField> sortFieldList = new ArrayList<>();
        final TemplateCategoryField sortTCF = new TemplateCategoryField(76, new ReportTemplate(),
                new Category(), new Field(), "", false);
        sortFieldList.add(sortTCF);
        sortFieldList.add(tcfList.get(0));

        final List<TemplateCategoryField> filterFieldList = new ArrayList<>();
        final TemplateCategoryField filterTCF = new TemplateCategoryField(67, new ReportTemplate(),
                new Category(), new Field(), "", false);
        filterFieldList.add(filterTCF);
        filterFieldList.add(tcfList.get(0));

        Mockito.when(mockReportTCUDTO.getReportName()).thenReturn("a report name");
        Mockito.when(mockReportTCUDTO.getTcfIdToStringSortList()).thenReturn(sortMap);
        Mockito.when(mockReportTCUDTO.getTcfIdToStringFilterList()).thenReturn(filterMap);
        Mockito.when(mockReportTCUDTO.getSortSelectedIds()).thenReturn(sortList);
        Mockito.when(mockReportTCUDTO.getFilterSelectedIds()).thenReturn(filterList);
        Mockito.when(mockReportTCUDTO.getSelectedTemplateCategoryFieldIds()).thenReturn(selectedTemplateIdList);
        Mockito.when(mockReportTemplateDAO.findTcfsById(selectedTemplateIdList)).thenReturn(tcfList);
        Mockito.when(mockReportTemplateDAO.findTcfsById(sortList)).thenReturn(sortFieldList);
        Mockito.when(mockReportTemplateDAO.findTcfsById(filterList)).thenReturn(filterFieldList);

        Mockito.when(mockReportTemplateDAO.findTemplateUserById(localTemplateUser.getId())).thenReturn(localTemplateUser);
        Mockito.when(mockReportTemplateDAO.checkSavedReportName(
                mockReportTCUDTO.getReportName(),
                this.templateUserId,
                false,
                user.getId())).thenReturn(false);

        final ReportTemplateMetadataDTO result = reportTemplateService.updateUserReport(
                user,
                mockReportTCUDTO,
                111);

        Assert.assertNotNull(result);
        Assert.assertNotNull(localTemplateUser.getLastUpdateTime());
        Assert.assertEquals(localTemplateUser.getName(), mockReportTCUDTO.getReportName());
        Assert.assertEquals(localTemplateUser.getUser(), user);
        Mockito.verify(mockReportTemplateDAO).findTemplateUserById(localTemplateUser.getId());
        Mockito.verify(mockReportTemplateDAO).updateEntity(localTemplateUser);
        Mockito.verify(mockReportTemplateDAO, Mockito.times(3)).createEntity(Mockito.any(BaseEntity.class));
    }

    @Test
    public void testDeleteUsersReport() {
        final List<TemplateUserSelection> list = new ArrayList<>();
        final TemplateUserSelection templateUserSelection1 = new TemplateUserSelection();
        final TemplateUserFilterSelection templateUserFilterSelection = new TemplateUserFilterSelection();
        templateUserFilterSelection.setId(10);
        final TemplateUserSortSelection templateUserSortSelection = new TemplateUserSortSelection();
        templateUserSortSelection.setId(11);
        templateUserSelection1.setUserFilterSelection(templateUserFilterSelection);
        templateUserSelection1.setUserSortSelection(templateUserSortSelection);
        list.add(templateUserSelection1);

        Mockito.when(templateUser.getId()).thenReturn(10);
        Mockito.when(mockReportTemplateDAO.findTemplateUserSelectionsById(templateUser.getId())).thenReturn(list);

        final BooleanResultDTO result = reportTemplateService.deleteUsersReport(templateUser.getId());

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isResult());
        Mockito.verify(mockReportTemplateDAO).findTemplateUserById(templateUser.getId());
        Mockito.verify(mockReportTemplateDAO).findTemplateUserSelectionsById(templateUser.getId());
        Mockito.verify(mockReportTemplateDAO).findTemplateUserSortSelectionsByTemplateUserSelection(
                Mockito.anyListOf(Integer.class));
        Mockito.verify(mockReportTemplateDAO).findTemplateUserFilterSelectionsByTemplateUserSelection(Mockito.anyListOf(Integer.class));
        Mockito.verify(mockReportTemplateDAO, Mockito.atLeastOnce()).deleteEntity(Mockito.any(TemplateUserFilterSelection.class));
        Mockito.verify(mockReportTemplateDAO, Mockito.atLeastOnce()).deleteEntity(Mockito.any(TemplateUserSortSelection.class));
        Mockito.verify(mockReportTemplateDAO, Mockito.atLeastOnce()).deleteEntity(Mockito.any(TemplateUserSelection.class));
        Mockito.verify(mockReportTemplateDAO).deleteEntity(templateUser);
    }

    @Test
    public void testGetStaticListByTcf() {
        final TemplateCategoryField tcf = this.tcfList.get(0);
        final List<Object> list =  new ArrayList<>();
        final String column = "columnName";

        Mockito.when(mockReportTemplateDAO.findListByClass(
                tcf.getField().getFieldProcessingEnum().getStaticClassName(),
                column)).thenReturn(list);

        List<Object> results = reportTemplateService.getStaticListByTcf(tcfIdMagicNumber, column);

        Assert.assertNotNull(results);
        Assert.assertTrue(results == list);
        Mockito.verify(mockReportTemplateDAO).findById(TemplateCategoryField.class, tcfIdMagicNumber);
        Mockito.verify(mockReportTemplateDAO).findListByClass(
                tcf.getField().getFieldProcessingEnum().getStaticClassName(),
                column);
    }

    @Test
    public void testGetEnumListByTcf() {
        final Field localField = new Field(11, "user", "role", "fieldDisplay");
        final TemplateCategoryField tcf = new TemplateCategoryField(
                this.tcfIdMagicNumber, this.reportTemplate, this.category, localField, "", true);

        Mockito.when(mockReportTemplateDAO.findById(TemplateCategoryField.class, 11)).thenReturn(tcf);

        final List<Field.HasReportFiltersNameAndIdPair> results = reportTemplateService.getEnumListByTcf(11);

        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        Mockito.verify(mockReportTemplateDAO).findById(TemplateCategoryField.class, 11);
    }

    @Test
    public void testGetListsByField() {
        final String tableName = this.tcfList.get(0).getField().getTable();
        final String columnName = this.tcfList.get(0).getField().getColumn();
        final List<Object> list = new ArrayList<>();

        Mockito.when(mockReportTemplateDAO.findById(TemplateCategoryField.class, tcfIdMagicNumber)).thenReturn(this.tcfList.get(0));
        Mockito.when(mockReportTemplateDAO.findListByColumn(tableName, columnName, "filterBy", "\'selectedTerms\'"))
                .thenReturn(list);

        final List<Object> results = this.reportTemplateService.getListsByField(tcfIdMagicNumber, "filterBy", "selectedTerms");

        Assert.assertTrue(results == list);
        Mockito.verify(mockReportTemplateDAO).findById(TemplateCategoryField.class, tcfIdMagicNumber);
        Mockito.verify(mockReportTemplateDAO).findListByColumn(tableName, columnName, "filterBy", "\'selectedTerms\'");
    }

    @Test
    public void testGetListsByFieldEmptyString() {
        final String tableName = this.tcfList.get(0).getField().getTable();
        final String columnName = this.tcfList.get(0).getField().getColumn();
        final List<Object> list = new ArrayList<>();

        Mockito.when(mockReportTemplateDAO.findListByColumn(tableName, columnName, "filterBy", ""))
                .thenReturn(list);

        final List<Object> results = this.reportTemplateService.getListsByField(tcfIdMagicNumber, "filterBy", "");

        Assert.assertTrue(results == list);
        Mockito.verify(mockReportTemplateDAO).findById(TemplateCategoryField.class, tcfIdMagicNumber);
        Mockito.verify(mockReportTemplateDAO).findListByColumn(tableName, columnName, "filterBy", "");
    }
}
