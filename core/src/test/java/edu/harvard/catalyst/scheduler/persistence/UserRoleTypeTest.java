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

import java.util.Optional;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * 
 * @author clint
 * @date Jul 31, 2013
 * 
 */
public final class UserRoleTypeTest extends TestCase {

    @Test
    public void testDatabaseIds() {
        TestCase.assertEquals(1, UserRoleType.STUDY_COORDINATOR_RESEARCH_ASSISTANT.databaseId);
        TestCase.assertEquals(2, UserRoleType.PI.databaseId);
        TestCase.assertEquals(3, UserRoleType.CO_INVESTIGATOR.databaseId);
        TestCase.assertEquals(4, UserRoleType.OTHER_STAFF.databaseId);
        TestCase.assertEquals(5, UserRoleType.NURSE.databaseId);
        TestCase.assertEquals(6, UserRoleType.NUTRITIONIST.databaseId);
        TestCase.assertEquals(7, UserRoleType.ADMINISTRATION.databaseId);
        TestCase.assertEquals(8, UserRoleType.LAB.databaseId);
        TestCase.assertEquals(9, UserRoleType.ADMINISTRATIVE_DIRECTOR.databaseId);
        TestCase.assertEquals(10, UserRoleType.NURSING_MANAGER.databaseId);
        TestCase.assertEquals(11, UserRoleType.NUTRITION_MANAGER.databaseId);
        TestCase.assertEquals(12, UserRoleType.FINAL_APPROVER.databaseId);
        TestCase.assertEquals(13, UserRoleType.PHYSICIAN.databaseId);
        TestCase.assertEquals(14, UserRoleType.PHYSICIAN2.databaseId);
        TestCase.assertEquals(15, UserRoleType.SCHEDULER.databaseId);
        TestCase.assertEquals(16, UserRoleType.SCHEDULER2.databaseId);
        TestCase.assertEquals(17, UserRoleType.PROTOCOL_NURSE.databaseId);
        TestCase.assertEquals(18, UserRoleType.ASSOCIATE_NURSE.databaseId);
        TestCase.assertEquals(19, UserRoleType.PROTOCOL_NUTRITIONIST.databaseId);
    }

    @Test
    public void testHumanReadableName() {
        TestCase.assertEquals("Study Coordinator - Research Asst", UserRoleType.STUDY_COORDINATOR_RESEARCH_ASSISTANT.humanReadableName);
        TestCase.assertEquals("PI", UserRoleType.PI.humanReadableName);
        TestCase.assertEquals("Co-Investigator", UserRoleType.CO_INVESTIGATOR.humanReadableName);
        TestCase.assertEquals("Other Study Staff", UserRoleType.OTHER_STAFF.humanReadableName);
        TestCase.assertEquals("Nurse", UserRoleType.NURSE.humanReadableName);
        TestCase.assertEquals("Nutritionist", UserRoleType.NUTRITIONIST.humanReadableName);
        TestCase.assertEquals("CRC Administration", UserRoleType.ADMINISTRATION.humanReadableName);
        TestCase.assertEquals("Lab", UserRoleType.LAB.humanReadableName);
        TestCase.assertEquals("Administrative Director", UserRoleType.ADMINISTRATIVE_DIRECTOR.humanReadableName);
        TestCase.assertEquals("Nurse Manager Director", UserRoleType.NURSING_MANAGER.humanReadableName);
        TestCase.assertEquals("Nutrition Manager", UserRoleType.NUTRITION_MANAGER.humanReadableName);
        TestCase.assertEquals("Final Approver", UserRoleType.FINAL_APPROVER.humanReadableName);
        TestCase.assertEquals("Physician", UserRoleType.PHYSICIAN.humanReadableName);
        TestCase.assertEquals("Physician2", UserRoleType.PHYSICIAN2.humanReadableName);
        TestCase.assertEquals("Scheduler", UserRoleType.SCHEDULER.humanReadableName);
        TestCase.assertEquals("Scheduler2", UserRoleType.SCHEDULER2.humanReadableName);
        TestCase.assertEquals("Protocol Nurse", UserRoleType.PROTOCOL_NURSE.humanReadableName);
        TestCase.assertEquals("Associate Nurse", UserRoleType.ASSOCIATE_NURSE.humanReadableName);
        TestCase.assertEquals("Protocol Nutritionist", UserRoleType.PROTOCOL_NUTRITIONIST.humanReadableName);
    }

    @Test
    public void testFromHumanReadableName() {
        assertEquals(Optional.empty(), UserRoleType.fromHumanReadableName(null));
        assertEquals(Optional.empty(), UserRoleType.fromHumanReadableName(""));
        assertEquals(Optional.empty(), UserRoleType.fromHumanReadableName("salkdalksd"));

        TestCase.assertEquals(UserRoleType.STUDY_COORDINATOR_RESEARCH_ASSISTANT, UserRoleType.fromHumanReadableName("Study Coordinator - Research Asst").get());
        TestCase.assertEquals(UserRoleType.PI, UserRoleType.fromHumanReadableName("PI").get());
        TestCase.assertEquals(UserRoleType.CO_INVESTIGATOR, UserRoleType.fromHumanReadableName("Co-Investigator").get());
        TestCase.assertEquals(UserRoleType.OTHER_STAFF, UserRoleType.fromHumanReadableName("Other Study Staff").get());
        TestCase.assertEquals(UserRoleType.NURSE, UserRoleType.fromHumanReadableName("Nurse").get());
        TestCase.assertEquals(UserRoleType.NUTRITIONIST, UserRoleType.fromHumanReadableName("Nutritionist").get());
        TestCase.assertEquals(UserRoleType.ADMINISTRATION, UserRoleType.fromHumanReadableName("CRC Administration").get());
        TestCase.assertEquals(UserRoleType.LAB, UserRoleType.fromHumanReadableName("Lab").get());
        TestCase.assertEquals(UserRoleType.ADMINISTRATIVE_DIRECTOR, UserRoleType.fromHumanReadableName("Administrative Director").get());
        TestCase.assertEquals(UserRoleType.NURSING_MANAGER, UserRoleType.fromHumanReadableName("Nurse Manager Director").get());
        TestCase.assertEquals(UserRoleType.NUTRITION_MANAGER, UserRoleType.fromHumanReadableName("Nutrition Manager").get());
        TestCase.assertEquals(UserRoleType.FINAL_APPROVER, UserRoleType.fromHumanReadableName("Final Approver").get());
        TestCase.assertEquals(UserRoleType.PHYSICIAN, UserRoleType.fromHumanReadableName("Physician").get());
        TestCase.assertEquals(UserRoleType.PHYSICIAN2, UserRoleType.fromHumanReadableName("Physician2").get());
        TestCase.assertEquals(UserRoleType.SCHEDULER, UserRoleType.fromHumanReadableName("Scheduler").get());
        TestCase.assertEquals(UserRoleType.SCHEDULER2, UserRoleType.fromHumanReadableName("Scheduler2").get());
        TestCase.assertEquals(UserRoleType.PROTOCOL_NURSE, UserRoleType.fromHumanReadableName("Protocol Nurse").get());
        TestCase.assertEquals(UserRoleType.ASSOCIATE_NURSE, UserRoleType.fromHumanReadableName("Associate Nurse").get());
        TestCase.assertEquals(UserRoleType.PROTOCOL_NUTRITIONIST, UserRoleType.fromHumanReadableName("Protocol Nutritionist").get());
    }
}
