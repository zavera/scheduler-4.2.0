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

import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author clint
 * 
 * TODO: NB: It would be VERY nice to be able to use 
 *  javax.xml.bind.DatatypeConverter.{parseBase64Binary, printBase64Binary},
 *  but we can't.  OneWayPasswordEncoder (and all the existing hashed 
 *  passwords!) rely on the behavior of sun.misc.BASE64Decoder when 
 *  that class is fed non-base64-encoded salt strings. <sad face>
 */
public final class Base64 {
    private Base64() {
        super();
    }

    public static String base64Encode(final byte[] encValue) {
        String stringResult = null;
        byte[] bytesResult = java.util.Base64.getMimeEncoder().encode(encValue);
        try {
            stringResult = new String(bytesResult, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SchedulerRuntimeException.logAndThrow("Cannot encode", e);
        }
        return stringResult;
    }

    public static byte[] base64Decode(final String encryptedValue) {
        byte[] bytesResult = java.util.Base64.getMimeDecoder().decode(encryptedValue);

        return bytesResult;
    }
}
