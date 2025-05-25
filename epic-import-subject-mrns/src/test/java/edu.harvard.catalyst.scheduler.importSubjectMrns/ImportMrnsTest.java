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
package edu.harvard.catalyst.scheduler.importSubjectMrns;

import com.google.common.collect.Sets;
import com.opencsv.CSVReader;
import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.FactoryMockKey;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.List;

import static edu.harvard.catalyst.scheduler.util.TestUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ImportMrnsTest {


    SubjectDAO subjectDAO = mock(SubjectDAO.class);
    Subject subject1 = mock(Subject.class);
    Subject subject2 = mock(Subject.class);
    Subject subject3 = mock(Subject.class);
    Subject subject4 = mock(Subject.class);

    Key key;

    ImportSubjectMrns importSubjectMrns;

    @Mock
    private Appender mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;


    CSVReader getCsvReader(String filenameOnClasspath) throws UnsupportedEncodingException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filenameOnClasspath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        CSVReader csvReader = new CSVReader(bufferedReader);

        return csvReader;
    }

    @Before
    public void setup() {
        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);

        FactoryMockKey keyFactory = new FactoryMockKey();
        key = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(key);

        importSubjectMrns = new ImportSubjectMrns(subjectDAO, key);

        importSubjectMrns.loadIndices(0, 1, 2, 3, 4);
        importSubjectMrns.setClobberMrns(false);

        when(subjectDAO.findById(1)).thenReturn(subject1);
        when(subjectDAO.findById(2)).thenReturn(subject2);
        when(subjectDAO.findById(3)).thenReturn(subject3);
        when(subjectDAO.findById(4)).thenReturn(subject4);

        SubjectMrn subjectMrn1 = new SubjectMrn(subject1, SubjectDataEncryptor.encrypt("blah"), "mgh", "N");
        SubjectMrn subjectMrn2 = new SubjectMrn(subject2, SubjectDataEncryptor.encrypt("foo"), "mgh", "N");
        SubjectMrn subjectMrn3 = new SubjectMrn(subject3, SubjectDataEncryptor.encrypt("bar"), "mgh", "N");

        when(subject1.getSubjectMrnSet()).thenReturn(Sets.newHashSet(subjectMrn1));
        when(subject2.getSubjectMrnSet()).thenReturn(Sets.newHashSet(subjectMrn2));
        when(subject3.getSubjectMrnSet()).thenReturn(Sets.newHashSet(subjectMrn3));
    }

    @Test
    public void testMaxIndex() {
        assertEquals("", 4, importSubjectMrns.getMaxIndex());

        importSubjectMrns.loadIndices(4, 6, 2, 3, 1);
        assertEquals("", 6, importSubjectMrns.getMaxIndex());
    }
    @Test
    public void testSunnyPreviousPuids() throws Exception {

        when(subject1.getPuid()).thenReturn(SubjectDataEncryptor.encrypt("P1"));
        when(subject2.getPuid()).thenReturn(SubjectDataEncryptor.encrypt("P2"));
        when(subject3.getPuid()).thenReturn(SubjectDataEncryptor.encrypt("bogus"));

        CSVReader csvReader = getCsvReader("sunny.csv");

        importSubjectMrns.loopThroughCsv(csvReader);

        verify(subjectDAO, times(1)).createEntity(any(SubjectMrn.class));

        // 3x . two for subjectMrn, one for subject (puid --> bogus)
        verify(subjectDAO, times(3)).updateEntity(any(BaseEntity.class));

        verifyLog("3 successes out of 3 data rows", 1, mockAppender, captorLoggingEvent);
    }
    @Test
    public void testSunny() throws Exception {

        CSVReader csvReader = getCsvReader("sunny.csv");

        importSubjectMrns.loopThroughCsv(csvReader);

        verify(subjectDAO, times(1)).createEntity(any(SubjectMrn.class));

        // 5x . two for subjectMrn, three for subject (puid)
        verify(subjectDAO, times(5)).updateEntity(any(BaseEntity.class));

        verifyLog("3 successes out of 3 data rows", 1, mockAppender, captorLoggingEvent);
    }
    @Test
    public void testRainy() throws Exception {

        CSVReader csvReader = getCsvReader("rainy.csv");

        SubjectMrn subjectMrn1 = new SubjectMrn(subject2, SubjectDataEncryptor.encrypt("foo"), "mgh", "N");
        SubjectMrn subjectMrn2 = new SubjectMrn(subject2, SubjectDataEncryptor.encrypt("bar"), "mgh", "N");

        when(subject2.getSubjectMrnSet()).thenReturn(Sets.newHashSet(subjectMrn1, subjectMrn2));


        importSubjectMrns.loopThroughCsv(csvReader);

        verify(subjectDAO, times(0)).createEntity(any(SubjectMrn.class));

        List<LoggingEvent> events = getLogEvents(0, mockAppender, captorLoggingEvent);

        verifyEvents("row number 0 has 4 values instead of at least 5", 1, events);
        verifyEvents("Row: 1. No subject found with id: 7", 1, events);
        verifyEvents("Row: 2. This subject's MRN-code differs between the DB and the CSV", 1, events);
        verifyEvents("Row: 3. Invalid subject id: NaN", 1, events);
        verifyEvents("Row: 4. This subject id: 2, has >1 previous MRN", 1, events);
        verifyEvents("0 successes out of 5 data rows", 1, events);
    }
    @Test
    public void testRainyWithClobber() throws Exception {

        CSVReader csvReader = getCsvReader("rainy.csv");

        SubjectMrn subjectMrn1 = new SubjectMrn(subject2, SubjectDataEncryptor.encrypt("foo"), "mgh", "N");
        SubjectMrn subjectMrn2 = new SubjectMrn(subject2, SubjectDataEncryptor.encrypt("bar"), "mgh", "N");

        when(subject2.getSubjectMrnSet()).thenReturn(Sets.newHashSet(subjectMrn1, subjectMrn2));

        importSubjectMrns.setClobberMrns(true);
        importSubjectMrns.loopThroughCsv(csvReader);

        verify(subjectDAO, times(0)).createEntity(any(SubjectMrn.class));

        List<LoggingEvent> events = getLogEvents(0, mockAppender, captorLoggingEvent);

        verifyEvents("row number 0 has 4 values instead of at least 5", 1, events);
        verifyEvents("Row: 1. No subject found with id: 7", 1, events);
        verifyEvents("Row: 3. Invalid subject id: NaN", 1, events);
        verifyEvents("Row: 4. This subject id: 2, has >1 previous MRN", 1, events);
        verifyEvents("1 successes out of 5 data rows", 1, events);
    }

}
