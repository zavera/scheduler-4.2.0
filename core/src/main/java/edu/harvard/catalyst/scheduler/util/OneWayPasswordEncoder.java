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
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class OneWayPasswordEncoder {

    private static final Logger LOG = Logger.getLogger(OneWayPasswordEncoder.class);

    /**
     * Count for the number of time to hash,
     * more you hash more difficult it would be for the attacker
     */
    private static final int COUNT = 5;

    private OneWayPasswordEncoder() {
    }
    
    private static final class Holder {
        private static final OneWayPasswordEncoder instance = makeInstance();
        
        static OneWayPasswordEncoder makeInstance() {
            if(LOG.isDebugEnabled()) {
                LOG.debug("getInstance() - start");
            }

            if(instance == null) {
                final OneWayPasswordEncoder returnOneWayPasswordEncoder = new OneWayPasswordEncoder();
                LOG.info("New instance created");
                if(LOG.isDebugEnabled()) {
                    LOG.debug("getInstance() - end");
                }
                return returnOneWayPasswordEncoder;
            } else {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("getInstance() - end");
                }
                return instance;
            }
        }
    }

    public static OneWayPasswordEncoder getInstance() {
        return Holder.instance;
    }

    public synchronized String encode(final String password, final String saltKey) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("encode(String, String) - start");
        }

        String encodedPassword = null;
        final byte[] salt = base64ToByte(saltKey);
        final String encodeProblem = "Problem encoding password!";

        byte[] btPass = null;
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt);

            btPass = digest.digest(password.getBytes("UTF-8"));
            for (int i = 0; i < COUNT; i++) {
                digest.reset();
                btPass = digest.digest(btPass);
            }
        }
        catch (final UnsupportedEncodingException uee) {
            SchedulerRuntimeException.logAndThrow(encodeProblem, uee);
        }
        catch (final NoSuchAlgorithmException nsae) {
            SchedulerRuntimeException.logAndThrow(encodeProblem, nsae);
        }

        encodedPassword = byteToBase64(btPass);

        if(LOG.isDebugEnabled()) {
            LOG.debug("encode(String, String) - end");
        }
        return encodedPassword;
    }

    byte[] base64ToByte(final String str) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("base64ToByte(String) - start");
        }

        final byte[] returnbyteArray = Base64.base64Decode(str);

        if(LOG.isDebugEnabled()) {
            LOG.debug("base64ToByte(String) - end");
        }

        return returnbyteArray;
    }

    public String byteToBase64(final byte[] bt) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("byteToBase64(byte[]) - start");
        }

        final String returnString = Base64.base64Encode(bt);

        if(LOG.isDebugEnabled()) {
            LOG.debug("byteToBase64(byte[]) - end");
        }

        return returnString;
    }
}
