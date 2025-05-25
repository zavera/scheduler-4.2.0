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
package edu.harvard.catalyst.scheduler.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class OneWayPasswordEncoderTest {
    @Test
    public void testEncode() {
        //NB: Test credentials from test data set
        final String salt = "/3/Nn3HMS201";

        final String password = "Bostonbox8)";
        
        final String encoded = OneWayPasswordEncoder.getInstance().encode(password, salt);
        
        final String expected = "bqVlH9MHUDBIaNuM3uDsYOH3GHDuXw6nWOJq92UJzzA=";

        assertEquals(expected, encoded);
    }

    @Test
    public void testEncodeUserSa123() {
        //NB: Test credentials from test data set
        final String salt = "cb860315/b2c7/4f08/9686/0416b1bca567";
        
        final String password = "Chickbox8)";
        
        final String encoded = OneWayPasswordEncoder.getInstance().encode(password, salt);
        
        final String expected = "VotytAHOad+oVM5/rlfVWUA1McpbqNuNEYPYMeeonbg=";

        assertEquals(expected, encoded);
    }
    
    @Test
    public void testEncodeUserNr123() {
        //NB: Test credentials from test data set
        final String salt = "5cc3c5d7/3574/4d31/a562/bbe98957f882";
        
        //final String password = "Bickbox8)";
        final String password = "Chickbox8)";
        
        final String encoded = OneWayPasswordEncoder.getInstance().encode(password, salt);
        
        final String expected = "TRXsHZAYVf9KR6FTKKR95mM+tsX8wZ4aNVR+BwxoM0A=";

        assertEquals(expected, encoded);
    }
    
    @Test
    public void testEncodeUserFd123() {
        //NB: Test credentials from test data set
        final String salt = "2038e87f/b79d/46f9/82dd/bdc60c19f88d";
        
        final String password = "Chickbox8)";
        
        final String encoded = OneWayPasswordEncoder.getInstance().encode(password, salt);
        
        final String expected = "uIJbe0fCoMabO5hxpa7ZYqq53Pzmylgd4KSNdBL9Hrg=";

        assertEquals(expected, encoded);
    }
    
    @Test
    public void testEncodeUserFd123OnBick() {
        //NB: Test credentials from test data set
        final String salt = "93b88a1a/b54f/490f/959b/99ad80eeb53c";
        
        final String password = "Chickbox8)";
        
        final String encoded = OneWayPasswordEncoder.getInstance().encode(password, salt);
        
        final String expected = "gOFsTn0IS4zenELfAq/Ss54CB02GKpPh1yxirq/EApE=";

        assertEquals(expected, encoded);
    }
}
