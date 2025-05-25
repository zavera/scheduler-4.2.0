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
package edu.harvard.catalyst.scheduler.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.catalyst.hccrc.core.util.TestHelpers;

public final class PermutationGeneratorTest extends Assert implements TestHelpers {
    @Test
    public void testConstructor() {
    	intercept(SchedulerRuntimeException.class, () -> new PermutationGenerator(0));
    	
    	intercept(SchedulerRuntimeException.class, () -> new PermutationGenerator(-1));
    	
    	intercept(SchedulerRuntimeException.class, () -> new PermutationGenerator(-10));
    	
    	final PermutationGenerator pg = new PermutationGenerator(5);
    	
    	assertEquals(120, pg.getTotal());
    	
    	assertEquals(120, pg.getNumLeft());
    	
    	final int[] a = pg.getA();
    	
    	assertEquals(5, a.length);
    	
		assertEquals(0, a[0]);
		assertEquals(1, a[1]);
		assertEquals(2, a[2]);
		assertEquals(3, a[3]);
		assertEquals(4, a[4]);
    }
    
    @Test
    public void testHasMoreNextAndReset() {
    	final PermutationGenerator pg = new PermutationGenerator(3);
    	
    	final Set<List<Integer>> perms = new HashSet<>();
    	
    	while(pg.hasMore()) {
    		final List<Integer> perm = new ArrayList<>();
    		
    		for(final int i : pg.getNext()) {
    			perm.add(i);
    		}
    		
    		perms.add(perm);
    	}
    	
    	@SuppressWarnings("unchecked")
		final Set<List<Integer>> expected = Sets.newHashSet(
    			Lists.newArrayList(0,1,2),
    			Lists.newArrayList(0,2,1),
    			Lists.newArrayList(1,0,2),
    			Lists.newArrayList(1,2,0),
    			Lists.newArrayList(2,0,1),
    			Lists.newArrayList(2,1,0)
    			);
    	
    	assertEquals(expected, perms);
    	
    	{
    		pg.reset();
    		
    		assertEquals(6, pg.getTotal());
        	
        	assertEquals(6, pg.getNumLeft());
        	
        	final int[] a = pg.getA();
        	
        	assertEquals(3, a.length);
        	
    		assertEquals(0, a[0]);
    		assertEquals(1, a[1]);
    		assertEquals(2, a[2]);
    	}
    }
}
