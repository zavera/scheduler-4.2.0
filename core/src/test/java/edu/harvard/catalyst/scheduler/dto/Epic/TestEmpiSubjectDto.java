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
package edu.harvard.catalyst.scheduler.dto.Epic;

import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.security.Key;
import java.util.Properties;

import static edu.harvard.catalyst.scheduler.util.PropertyHelper.getProperties;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public final class TestEmpiSubjectDto {

    @Mock private EpicSubjectService subjectService;
    @Mock private SubjectDAO subjectDAO;

    @Autowired @Qualifier(value = "encryptionKeyBatch") Key key;

    @Test
    public void twoPatients() {
        helper("jones2");
    }
    @Test
    public void edgePatients() {
        helper("noPatients");
        helper("emptyPatients");
    }

    @Test
    public void edgePhones() {
        helper("noPhones");
        helper("emptyPhones");
    }

    @Test
    public void edgeOtherPids() {
        helper("noOtherPid");
        helper("emptyOtherPid");
    }

    @Test
    /*
        Tests that when no match is found in EMPI patient list will be an empty list and not null
     */
    public void patientListNotNull()
    {
        String resultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<QueryReply>\n<Patients/>\n</QueryReply>";
        EmpiSubjectDto empiSubjectDto = EmpiSubjectDto.unmarshall(resultXml);

        assertNotNull(empiSubjectDto.getPatients().getPatientList());
    }

    void helper(String name) {

        Properties schedulerProps = getProperties("empiDto.properties", getClass());

        String xml1 = (String)schedulerProps.get(name);

        EmpiSubjectDto result = EmpiSubjectDto.unmarshall(xml1);

        String remarshal = result.marshall();

        xml1 = canonicalXml(xml1);
        remarshal = canonicalXml(remarshal);

        assertEquals("", xml1, remarshal);

        return;
    }

    String canonicalXml(String input) {
        String result = input.replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"\\?>", "");

        result = result.trim().replaceAll("\\s+", " ");
        result = result.replaceAll("\\s+/", "/");

        // regex magic to collapse, for all values of blah, <blah></blah> into <blah/>, including any attributes
        result = result.replaceAll("<(\\w+)( ?[^>]*)>\\s*</\\1>","<$1$2/>");

        return result;
    }
}
