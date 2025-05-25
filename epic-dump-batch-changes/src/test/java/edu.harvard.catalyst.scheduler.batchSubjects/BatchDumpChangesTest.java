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
package edu.harvard.catalyst.scheduler.batchSubjects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.entity.Ethnicity;
import edu.harvard.catalyst.scheduler.persistence.EpicSubjectDAO;
import edu.harvard.catalyst.scheduler.persistence.NightlyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.FactoryMockKey;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.util.TestUtils;
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

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

import static edu.harvard.catalyst.scheduler.dto.Epic.EmpiSubjectDto.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BatchDumpChangesTest {

    BatchDumpChanges batchDumpChanges;
    NightlyDAO nightlyDAO = mock(NightlyDAO.class);

    @Mock
    private Appender mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;


    @Before
    public void setup() {
        Logger root = Logger.getRootLogger();
        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);

        FactoryMockKey keyFactory = new FactoryMockKey();
        Key key = keyFactory.createKey();
        SubjectDataEncryptor.setEncryptionKey(key);

        batchDumpChanges = new BatchDumpChanges(nightlyDAO, key);
    }

    @Test
    public void testChanges() throws Exception {

        NightlyBatchChanges nbc1 = new NightlyBatchChanges();
        nbc1.setId(1);
        NightlyBatchChanges nbc2 = new NightlyBatchChanges();
        nbc1.setId(2);

        List<NightlyBatchChanges> nbcList = Lists.newArrayList(nbc1, nbc2);

        when(nightlyDAO.findAllChangeRecords(0)).thenReturn(nbcList);

        batchDumpChanges.run(0);

        TestUtils.verifyLog("Found 2 Records of Change, starting with record 0", 1, mockAppender, captorLoggingEvent);
    }

}
