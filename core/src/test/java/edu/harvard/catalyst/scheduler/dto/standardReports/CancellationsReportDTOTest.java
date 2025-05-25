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
package edu.harvard.catalyst.scheduler.dto.standardReports;

import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.dto.response.CancellationsReportResponseDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author carl
 *
 */
public class CancellationsReportDTOTest {
    private User u1;
    Study st;
    VisitTemplate vt;
    private BookedVisit b1Spy = spy(new BookedVisit());
    private BookedVisit b2Spy = spy(new BookedVisit());
    private Subject subjectSpy1 = spy(new Subject());
    private Subject subjectSpy2 = spy(new Subject());
    private Date scheduledStartTime = new Date(100060000);
    private Date scheduledEndTime = new Date(100120000);
    private Date cancelTime = new Date(100180000);
    private Date schedulingTime = new Date(100240000);

    @Before
    public void setup() {
        u1 = new User();

        u1.setFirstName("f1");
        u1.setMiddleName("m1");
        u1.setLastName("l1");
        u1.setEcommonsId("ecid1");

        VisitType vtype = new VisitType();
        vtype.setName("vtype");

        vt = new VisitTemplate();
        vt.setName("vt");
        vt.setVisitType(vtype);

        st = new Study();
        st.setName("study");
        st.setCatalystId("catId");
        st.setIrb("irb");
        st.setLocalId("lid");
        st.setInvestigator(u1);

        SubjectMrn subjectMrn1 = new SubjectMrn();
        subjectMrn1.setSubject(subjectSpy1);
        subjectMrn1.setMrn("1234");
        SubjectMrn subjectMrn2 = new SubjectMrn();
        subjectMrn2.setSubject(subjectSpy2);
        subjectMrn2.setMrn("5678");

        CancellationStatus cs1 = new CancellationStatus();
        cs1.setName("cs1");
        CancellationStatus cs2 = new CancellationStatus();
        cs2.setName("cs2");

        AppointmentStatusReason asr1 = new AppointmentStatusReason();
        asr1.setName("asr1");
        AppointmentStatusReason asr2 = new AppointmentStatusReason();
        asr2.setName("asr2");

        doReturn(subjectMrn1).when(b1Spy).getSubjectMrnDecrypted();
        doReturn(subjectMrn2).when(b2Spy).getSubjectMrnDecrypted();

        doReturn(vt).when(b1Spy).getVisitTemplate();
        doReturn(vt).when(b2Spy).getVisitTemplate();

        doReturn(st).when(b1Spy).getStudy();
        doReturn(st).when(b2Spy).getStudy();

        doReturn(asr1).when(b1Spy).getCancelStatusReason();
        doReturn(asr2).when(b2Spy).getCancelStatusReason();

        doReturn(cancelTime).when(b1Spy).getCancelDate();
        doReturn(cancelTime).when(b2Spy).getCancelDate();

        doReturn(cs1).when(b1Spy).getCancelStatus();
        doReturn(cs2).when(b2Spy).getCancelStatus();

        doReturn(schedulingTime).when(b1Spy).getSchedulingTime();
        doReturn(schedulingTime).when(b2Spy).getSchedulingTime();

        doReturn(scheduledStartTime).when(b1Spy).getScheduledStartTime();
        doReturn(scheduledStartTime).when(b2Spy).getScheduledStartTime();

        doReturn(scheduledEndTime).when(b1Spy).getScheduledEndTime();
        doReturn(scheduledEndTime).when(b2Spy).getScheduledEndTime();

        doReturn(subjectMrn1).when(b1Spy).getSubjectMrnDecrypted();
        doReturn(subjectMrn2).when(b2Spy).getSubjectMrnDecrypted();

        doReturn(u1).when(b1Spy).getSchedulingUser();
        doReturn(u1).when(b2Spy).getSchedulingUser();

        doReturn("sf1").when(subjectSpy1).getFirstName();
        doReturn("sm1").when(subjectSpy1).getMiddleName();
        doReturn("sl1").when(subjectSpy1).getLastName();

        doReturn("sf2").when(subjectSpy2).getFirstName();
        doReturn("sm2").when(subjectSpy2).getMiddleName();
        doReturn("sl2").when(subjectSpy2).getLastName();
    }

    @Test
    public void toCsvHeadersTest() {
        CancellationsReportResponseDTO dto1 = new CancellationsReportResponseDTO(b1Spy);

        String result = dto1.toCsvHeaders();

        assertEquals("Study Name,Catalyst ID,Local ID,IRB #,PI Name," +
                "Subject,MRN,Visit Name,Visit Type,Visit Start Time," +
                "Visit End Time,Visit Length,Scheduled Date,Cancel Date," +
                "Reason,Cancellation Type,User", result);
    }

    @Test
    public void toCsvRowsTest() {

        CancellationsReportResponseDTO dto1 = new CancellationsReportResponseDTO(b1Spy);
        CancellationsReportResponseDTO dto2 = new CancellationsReportResponseDTO(b2Spy);
        List<?> dtoList = Lists.newArrayList(dto1, dto2);

        List<String> threeRows = dto1.toCsvRows(dtoList);

        String expected1 = "\"study\",\"catId\",\"lid\",\"irb\"," +
                "\"f1 m1 l1\",\"sf1 sm1 sl1\",\"1234\"," +
                "\"vt\",\"vtype\"," +
                "\"01/01/1970 22:47\",\"01/01/1970 22:48\",\"1 Min\"," +
                "\"01/01/1970 22:50\",\"01/01/1970 22:49\"," +
                "\"asr1\",\"cs1\",\"ecid1\"\n";

        String expected2 = "\"study\",\"catId\",\"lid\",\"irb\"," +
                "\"f1 m1 l1\",\"sf2 sm2 sl2\",\"5678\"," +
                "\"vt\",\"vtype\"," +
                "\"01/01/1970 22:47\",\"01/01/1970 22:48\",\"1 Min\"," +
                "\"01/01/1970 22:50\",\"01/01/1970 22:49\"," +
                "\"asr2\",\"cs2\",\"ecid1\"\n";

        assertEquals(3, threeRows.size());
        
        assertEquals(expected1, threeRows.get(1));
        assertEquals(expected2, threeRows.get(2));
    }
}
