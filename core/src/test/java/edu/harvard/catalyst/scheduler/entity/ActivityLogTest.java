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
package edu.harvard.catalyst.scheduler.entity;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * 
 * @author clint
 * @date Aug 16, 2013
 *
 */
public final class ActivityLogTest extends TestCase {
    @Test
    public void testSetAction() {
        ActivityLog al = new ActivityLog();

        String long250 =
                "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111";
        String long255 =
                "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "61111";
        String long255dots =
                "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "6111111111" +
                        "7111111111" +
                        "8111111111" +
                        "9111111111" +
                        "A111111111" +
                        "1111111111" +
                        "2111111111" +
                        "3111111111" +
                        "4111111111" +
                        "5111111111" +
                        "61...";
        String long260 = long255 + "12345";

        assertEquals(260, long260.length());
        assertEquals(255, long255.length());
        assertEquals(255, long255dots.length());
        assertEquals(250, long250.length());

        al.setActionPerformed(long250);
        assertEquals(long250, al.getActionPerformed());

        al.setActionPerformed(long255);
        assertEquals(long255, al.getActionPerformed());

        al.setActionPerformed(long260);
        assertEquals(long255dots, al.getActionPerformed());
    }
    
}
