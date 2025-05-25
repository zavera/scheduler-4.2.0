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

import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.ASCENDING;
import static edu.harvard.catalyst.scheduler.persistence.SortStrategy.DESCENDING;
import static org.mockito.Mockito.*;

/**
 * 
 * @author clint
 * @date Jul 31, 2013
 *
 */
public final class SortStrategyTest extends TestCase {
    @Test
    public void testFilterId() { 
        assertEquals(1, ASCENDING.sortId);
        assertEquals(2, DESCENDING.sortId);
    }
    
    @Test
    public void testMakeOrder() {
        final String columnName1 = "foo";
        final String columnName2 = "bar";

        CriteriaQueryHelper helper = mock(CriteriaQueryHelper.class);

        ASCENDING.makeOrder(columnName1, helper);
        verify(helper, times(1)).orderAsc(columnName1);

        DESCENDING.makeOrder(columnName2, helper);
        verify(helper, times(1)).orderDesc(columnName2);
    }
    
    @Test
    public void testMakeHqlOrderBySubClause() {
        final String columnName = "foo";

        // NB: Inspecting output of toString() here is a bit dodgy, but it lets us verify internal state
        // of Order objects that is otherwise hidden.
        assertEquals(" ORDER BY foo ASC", ASCENDING.makeHqlOrderBySubClause(columnName));
        assertEquals(" ORDER BY foo DESC", DESCENDING.makeHqlOrderBySubClause(columnName));
        
        final String multipleColumns = "foo, bar";
        
        assertEquals(" ORDER BY foo, bar ASC", ASCENDING.makeHqlOrderBySubClause(multipleColumns));
        assertEquals(" ORDER BY foo, bar DESC", DESCENDING.makeHqlOrderBySubClause(multipleColumns));
    }
    
    @Test
    public void testFromIdString() {
        assertEquals(Optional.empty(), SortStrategy.fromIdString(null));
        assertEquals(Optional.empty(), SortStrategy.fromIdString(""));
        assertEquals(Optional.empty(), SortStrategy.fromIdString("aslkdjalsdj"));
        
        assertEquals(ASCENDING, SortStrategy.fromIdString("1").get());
        assertEquals(DESCENDING, SortStrategy.fromIdString("2").get());
    }
}
