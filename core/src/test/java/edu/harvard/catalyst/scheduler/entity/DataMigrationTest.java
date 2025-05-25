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
/*

1. set up db data
2. set up JSON input
3. call web resource with JSON input
4. check web resource return value
5. check db side-effects
6. clean up db (or roll-back tx)
 */

package edu.harvard.catalyst.scheduler.entity;

import edu.harvard.catalyst.scheduler.AbstractDbIntegrationTest;
import edu.harvard.catalyst.scheduler.dto.SubjectsDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.security.SchedulerSession;
import edu.harvard.catalyst.scheduler.security.SchedulerUserDetails;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import edu.harvard.catalyst.scheduler.web.SubjectResource;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by xavier on 5/9/16.
 */
public class DataMigrationTest extends AbstractDbIntegrationTest {

    @Autowired @Qualifier("encryptionKey")
    Key encryptionKey;

    Session hibernateSession;

    @Before
    public void setUp() {

        SubjectDataEncryptor.setEncryptionKey(encryptionKey);

        // do this at the top, because it is later needed for creating persistent entities in the same method
        hibernateSession = session();

    }

    Ethnicity createAndPersistOneEthnicityEntity() {

        Ethnicity ethnicity1 = new Ethnicity();
        ethnicity1.setName("test etqhnicity");
        hibernateSession.save(ethnicity1);

        return ethnicity1;

    }

    Gender createAndPersistOneGenderEntity() {

        Gender gender1 = new Gender();
        gender1.setName("test gender");
        gender1.setCode("M");
        hibernateSession.save(gender1);

        return gender1;

    }

    Race createAndPersistOneRace() {

        Race race1 = new Race();
        race1.setName("test race");
        hibernateSession.save(race1);

        return race1;

    }

    Subject createAndPersistOneSubject() {

        Subject subject = new Subject();

        subject.setActive(true);
        subject.setSecure(true);
        subject.setBirthdate(new Date());

        subject.setEthnicity(createAndPersistOneEthnicityEntity());
        subject.setGender(createAndPersistOneGenderEntity());
        subject.setGenderType(GenderType.DECLINED);
        subject.setRace(createAndPersistOneRace());

        subject.setFirstName("");
        subject.setLastName("");
        subject.setPrimaryContactNumber("");

        hibernateSession.save(subject);

        return subject;
    }

    Study createAndPersistOneStudy() {
        Study study = new Study();

        hibernateSession.save(study);

        return study;
    }


    @Test
    public void testSubjectQuery() {

        createAndPersistOneSubject();

        Query query = hibernateSession.createQuery("select s from Subject s");
        List<Subject> subjectList = query.list();
        return;
    }

    @Test
    public void testStudyQuery() {

        createAndPersistOneStudy();

        Query query = hibernateSession.createQuery("select st from Study st");
        List<Study> studyList = query.list();
        return;
    }

    @Test
    public void testBookedVisitQuery() {


        Query query = hibernateSession.createQuery("select bv from BookedVisit bv");
        List<Study> studyList = query.list();
        return;
    }
}
