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
package edu.harvard.catalyst.scheduler.web;

import com.google.common.collect.Lists;
import com.google.gson.*;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.dto.request.ReportTemplateCreateUsersDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateDTO;
import edu.harvard.catalyst.scheduler.entity.HasReportFiltersNameAndId;
import edu.harvard.catalyst.scheduler.entity.User;
import edu.harvard.catalyst.scheduler.entity.reporttemplate.Field;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.service.ReportTemplateService;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateMetadataDTO;
import edu.harvard.catalyst.scheduler.dto.response.ReportTemplateUsersDTO;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 2/13/14
 * Time: 9:56 AM
 */

@RunWith(MockitoJUnitRunner.class)
public class ReportTemplateResourceTest {
    ReportTemplateResource reportTemplateResource;

    private Gson gson;

    {
        GsonBuilder builder = new GsonBuilder();


        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        gson = builder.setExclusionStrategies(new ExclusionStrategy() {


            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

        }).create();

    }

    @Mock
    ReportTemplateService mockReportTemplateService;

    @Mock
    private User user;

    private String host = "host";

    @Before
    public void setUp() {
        reportTemplateResource = new ReportTemplateResource(mockReportTemplateService);

        SchedulerSession session = mock (SchedulerSession.class);
        SchedulerUserDetails userDetails = mock (SchedulerUserDetails.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(session.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(user);
        when(request.getRemoteHost()).thenReturn(host);

        reportTemplateResource.setSession(session);
        reportTemplateResource.setRequest(request);
    }

    String getJsonString() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("/test-report-json1.txt");
        if (stream == null) {
            SchedulerRuntimeException.logAndThrow("WTF !$#&%!%#. Cannot find the files");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        StringBuilder json1Builder = new StringBuilder();

        String strLine;
        while ((strLine = br.readLine()) != null) {
            json1Builder.append(strLine);
        }

        String json1String = json1Builder.toString();
        json1String = deSpace(json1String);

        return json1String;
    }
    @Test
    public void runReportTemplate() throws Exception {

        String json1String = getJsonString();

        when(mockReportTemplateService.getTemplateName(1, "Administrative", 1)).thenReturn("templateName");

        reportTemplateResource.runReportTemplate(json1String, 1, "Administrative", 1);

        verify(mockReportTemplateService).getTemplateName(1, "Administrative", 1);

        // the call to service.runReportTemplate gets invoked at a higher level, perhaps
        //   by Jersey, and so won't show up in the unit-test

        return;
    }

    String deSpace(String input) {
        return input.replace(" ", "");
    }

    @Test
    public void getReportTemplateList() throws Exception {
        when(mockReportTemplateService.getReportTemplateList(any(User.class)))
                .thenReturn(new ArrayList<ReportTemplateMetadataDTO>());

        String result = reportTemplateResource.getReportTemplateList();

        assertEquals("[]", result);

        return;
    }
    @Test
    public void getReportTemplate() throws Exception {
        when(mockReportTemplateService.getReportTemplate(any(Integer.class)))
                .thenReturn(new ReportTemplateDTO(123, null, null, null, null));

        String result = reportTemplateResource.getReportTemplate(0);

        assertEquals("{\"id\":123}", result);

        return;
    }

    @Test
    public void createUsersReport() throws Exception {
        ReportTemplateCreateUsersDTO reportTemplateCreateUsersDTO = new ReportTemplateCreateUsersDTO();
        List<Integer> templateCategoryFieldIds = Lists.newArrayList(new Integer(2), new Integer(3), new Integer(4));
        reportTemplateCreateUsersDTO.setSelectedTemplateCategoryFieldIds(templateCategoryFieldIds);
        ReportTemplateMetadataDTO reportTemplateMetadataDTO = new ReportTemplateMetadataDTO(42, 42, "joey", "", "joey", new Date());

        when(mockReportTemplateService.createUserReport(user, reportTemplateCreateUsersDTO, 42))
                .thenReturn(reportTemplateMetadataDTO);

        String result = reportTemplateResource.createUsersReport(
                "{\"templateCategoryOrderByFieldIds\": []," +
                        "\"templateCategoryDirection\": []," +
                        "\"templateCategoryFieldIds\": [2,3,4]}",
                42);

        assertEquals("", result);

        return;
    }

    @Test
    public void getUsersReport() throws Exception {
        when(mockReportTemplateService.getUsersReport(any(Integer.class)))
                .thenReturn(new ReportTemplateUsersDTO(123, null, null, null,
                        null, null, "report1", Lists.newArrayList(), Lists.newArrayList(),
                        Lists.newArrayList(), Lists.newArrayList()));

        String result = reportTemplateResource.getUsersReport(0);

        assertEquals("{\"name\":\"report1\",\"sortDTOList\":[],\"sortDirectionList\":[],\"filterDTOList\":[],\"filterExpressionDTOList\":[],\"id\":123}", result);

        return;
    }

    @Test
    public void getListsByField()
    {
        final Integer tcfId = 10;
        final String filterBy = "foo";
        final String selectedTerms = "foo";

        List<Object> matchList = new ArrayList<>();
        matchList.add("foobar");
        matchList.add("footest");

        String expectedResponseString = reportTemplateResource.gson.toJson(matchList);

        when(mockReportTemplateService.getListsByField(eq(tcfId), eq(filterBy), eq(selectedTerms)))
                .thenReturn(matchList);


        String responseString = reportTemplateResource.getListsByField(tcfId, filterBy, selectedTerms);

        assertEquals(expectedResponseString, responseString);
    }
}
