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

import java.util.Map;
import java.util.Optional;

import static edu.harvard.catalyst.hccrc.core.util.ListUtils.enrich;
import static edu.harvard.catalyst.hccrc.core.util.Pair.pair;
import static edu.harvard.catalyst.hccrc.core.util.Pairs.toMap;

/**
 * @author clint
 * @date Jul 31, 2013
 *
 */
public enum UserRoleType {
    STUDY_COORDINATOR_RESEARCH_ASSISTANT(1, "Study Coordinator - Research Asst"),
    PI(2, "PI"),
    CO_INVESTIGATOR(3, "Co-Investigator"),
    OTHER_STAFF(4, "Other Study Staff"),
    NURSE(5, "Nurse"),
    NUTRITIONIST(6, "Nutritionist"),
    ADMINISTRATION(7, "CRC Administration"),
    LAB(8, "Lab"),
    ADMINISTRATIVE_DIRECTOR(9, "Administrative Director"),
    NURSING_MANAGER(10, "Nurse Manager Director"),
    NUTRITION_MANAGER(11, "Nutrition Manager"),
    FINAL_APPROVER(12, "Final Approver"),
    PHYSICIAN(13, "Physician"),
    PHYSICIAN2(14, "Physician2"),
    SCHEDULER(15, "Scheduler"),
    SCHEDULER2(16, "Scheduler2"),
    PROTOCOL_NURSE(17, "Protocol Nurse"),
    ASSOCIATE_NURSE(18, "Associate Nurse"),
    PROTOCOL_NUTRITIONIST(19, "Protocol Nutritionist");

    @Deprecated
    public final int databaseId;

    public final String humanReadableName;

    private UserRoleType(final int databaseId, final String humanReadableName) {
        this.databaseId = databaseId;
        this.humanReadableName = humanReadableName;
    }
    
    public static Optional<UserRoleType> fromHumanReadableName(final String name) {
        if(name == null) {
            return Optional.empty();
        }
        
        final String lowercaseName = name.toLowerCase();
        
        return Optional.ofNullable(Holder.byHumanReadableName.get(lowercaseName));
    }
    
    //NB: Initialization-on-demand-holder idiom, to allow thread-safe lazy-init of byHumanReadableName
    private static final class Holder {
        private Holder() {
        }
        
        static final Map<String, UserRoleType> byHumanReadableName = makeByHumanReadableNameMap();

        private static Map<String, UserRoleType> makeByHumanReadableNameMap() {
            return toMap(enrich(values()).map(role -> pair(role.humanReadableName.toLowerCase(), role)));
        } 
    }
}
