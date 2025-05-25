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
/**
 *
 */
package edu.harvard.catalyst.scheduler.service;

/**
 * @author ak303
 *
 */

import java.util.ArrayList;
import java.util.List;

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

/**
 *
 * PermutationGenerator courtesy of : http://www.merriampark.com/perm.htm
 */
public final class PermutationGenerator {

	private final int[] a;
	private int numLeft;
	private final int total;

	//NB: For tests
	int[] getA() {
		return a;
	}

	//NB: For tests
	int getNumLeft() {
		return numLeft;
	}

	//NB: For tests
	int getTotal() {
		return total;
	}

	// -----------------------------------------------------------
	// Constructor. WARNING: Don't make n too large.
	// Recall that the number of permutations is n!
	// which can be very large, even when n is as small as 20 --
	// 20! = 2,432,902,008,176,640,000 and
	// 21! is too big to fit into a Java long, which is
	// why we use Integer instead.
	// ----------------------------------------------------------

	public PermutationGenerator(final int n) {
		if (n < 1) {
			SchedulerRuntimeException.logAndThrow("Min 1");
		}
		a = new int[n];
		total = getFactorial(n);
		reset();
	}

	// ------
	// Reset
	// ------

	void reset() {
		for (int i = 0; i < a.length; i++) {
			a[i] = i;
		}

		numLeft = total;
	}

	// -----------------------------
	// Are there more permutations?
	// -----------------------------

	boolean hasMore() {
		return numLeft > 0;
	}

	// ------------------
	// Compute factorial
	// ------------------

	private static Integer getFactorial(final int n) {
		int fact = 1;
		for (int i = n; i > 1; i--) {
			fact = fact * i;
		}
		return fact;
	}

	// --------------------------------------------------------
	// Generate next permutation (algorithm from Rosen p. 284)
	// --------------------------------------------------------

	private static final List<Integer> toList(final int[] is) {
	    final List<Integer> result = new ArrayList<>(is.length);
	    
	    for(final int i : is) {
	        result.add(i);
	    }
	    
	    return result;
	}
	
	public List<Integer> getNext() {

		if (numLeft == total) {
			numLeft = numLeft - 1;
			return toList(a);
		}

		int temp;

		// Find largest index j with a[j] < a[j+1]

		int j = a.length - 2;
		while (a[j] > a[j + 1]) {
			j--;
		}

		// Find index k such that a[k] is smallest integer
		// greater than a[j] to the right of a[j]

		int k = a.length - 1;
		while (a[j] > a[k]) {
			k--;
		}

		// Interchange a[j] and a[k]

		temp = a[k];
		a[k] = a[j];
		a[j] = temp;

		// Put tail end of permutation after jth position in increasing order

		int r = a.length - 1;
		int s = j + 1;

		while (r > s) {
			temp = a[s];
			a[s] = a[r];
			a[r] = temp;
			r--;
			s++;
		}

		numLeft--;

		return toList(a);
	}
}
