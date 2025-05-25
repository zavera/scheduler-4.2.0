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

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Optional;

import static edu.harvard.catalyst.scheduler.persistence.UserDataReportFilterStrategy.*;
import static org.mockito.Mockito.*;

/**
 * 
 * @author clint
 * @date Jul 31, 2013
 *
 */
public final class UserDataReportFilterStrategyTest extends TestCase {
    @Test
    public void testDatabaseId() {
        assertEquals(1, LASTNAME.databaseId);
        assertEquals(2, INSTITUTION_ROLE.databaseId);
        assertEquals(3, ROLE.databaseId);
        assertEquals(4, IS_ACTIVE.databaseId);
        assertEquals(5, IS_NOT_ACTIVE.databaseId);
    }
    
    @Test
    public void testFromIdString() {
        assertEquals(Optional.empty(), UserDataReportFilterStrategy.fromIdString(null));
        assertEquals(Optional.empty(), UserDataReportFilterStrategy.fromIdString(""));
        assertEquals(Optional.empty(), UserDataReportFilterStrategy.fromIdString("aslkdjalsdj"));
        
        assertEquals(LASTNAME, UserDataReportFilterStrategy.fromIdString("1").get());
        assertEquals(INSTITUTION_ROLE, UserDataReportFilterStrategy.fromIdString("2").get());
        assertEquals(ROLE, UserDataReportFilterStrategy.fromIdString("3").get());
        assertEquals(IS_ACTIVE, UserDataReportFilterStrategy.fromIdString("4").get());
        assertEquals(IS_NOT_ACTIVE, UserDataReportFilterStrategy.fromIdString("5").get());
    }
    
    @Test
    public void testMakeCriterion() {

        CriteriaQueryHelper helper = mock(CriteriaQueryHelper.class);

        LASTNAME.makeCriterion(null, "foo", helper);
        verify(helper, times(1)).whereLike("lastName", "foo");

        IS_ACTIVE.makeCriterion(null, "ignored", helper);
        verify(helper, times(1)).whereEquals("active", Boolean.TRUE);

        IS_NOT_ACTIVE.makeCriterion(null, "ignored", helper);
        verify(helper, times(1)).whereEquals("active", Boolean.FALSE);

        //TODO: TEST {INSTITUTION_ROLE,ROLE}.makeCriterion()
    }
}
