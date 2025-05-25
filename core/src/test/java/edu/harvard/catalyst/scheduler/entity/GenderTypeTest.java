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

import edu.harvard.catalyst.scheduler.util.MiscUtil;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by xavier on 6/18/16.
 */
public class GenderTypeTest {

    GenderType f = GenderType.F;
    GenderType f2 = GenderType.F;
    GenderType fm = GenderType.FM;
    GenderType m = GenderType.M;
    GenderType mf = GenderType.MF;
    GenderType d = GenderType.DECLINED;
    GenderType u = GenderType.UNREPORTED;

    @Test
    public void testValues() {

        GenderType[] values = GenderType.values();
        assertEquals(6, values.length);

    }


    @Test
    public void testCannotShareResource() {

        assertTrue(f.canShareResource());
        assertTrue(fm.canShareResource());
        assertTrue(m.canShareResource());
        assertTrue(mf.canShareResource());
        assertTrue(d.canShareResource());
        assertTrue(u.canShareResource());

    }


    // This is a test of MiscUtil.differentObjects();
    // i.e. does it apply correctly to GenderType values
    @Test
    public void testMiscUtil() {

        assertFalse(MiscUtil.differentObjects(f, f2));
        assertTrue(MiscUtil.differentObjects(f, m));
    }

}
