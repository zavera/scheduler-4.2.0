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

import java.io.*;

////////////// based on now-deprecated sun.misc.BASE64Decoder and ancestor /////////////

public class BASE64Decoder {
    class CEStreamExhausted extends RuntimeException {
    }

    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    byte[] decode_buffer = new byte[4];

    public byte[] decodeBuffer(String var1) throws IOException {
        byte[] var2 = var1.getBytes();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var2);
        ByteArrayOutputStream var4 = new ByteArrayOutputStream();
        this.decodeBuffer(var3, var4);
        return var4.toByteArray();
    }
    public void decodeBuffer(InputStream var1, OutputStream var2) throws IOException {
        PushbackInputStream var5 = new PushbackInputStream(var1);

        while(true) {
            try {
                int var6 = this.decodeLinePrefix(var5, var2);

                int var3;
                for(var3 = 0; var3 + this.bytesPerAtom() < var6; var3 += this.bytesPerAtom()) {
                    this.decodeAtom(var5, var2, this.bytesPerAtom());
                }

                if (var3 + this.bytesPerAtom() == var6) {
                    this.decodeAtom(var5, var2, this.bytesPerAtom());
                } else {
                    this.decodeAtom(var5, var2, var6 - var3);
                }

                this.decodeLineSuffix(var5, var2);
            } catch (CEStreamExhausted var8) {
                return;
            }
        }
    }
    protected int decodeLinePrefix(PushbackInputStream var1, OutputStream var2) throws IOException {
        return this.bytesPerLine();
    }

    protected void decodeLineSuffix(PushbackInputStream var1, OutputStream var2) throws IOException {
    }

    protected int bytesPerAtom() {
        return 4;
    }

    protected int bytesPerLine() {
        return 72;
    }
    protected int readFully(InputStream var1, byte[] var2, int var3, int var4) throws IOException {
        for(int var5 = 0; var5 < var4; ++var5) {
            int var6 = var1.read();
            if (var6 == -1) {
                return var5 == 0 ? -1 : var5;
            }

            var2[var5 + var3] = (byte)var6;
        }

        return var4;
    }


    protected void decodeAtom(PushbackInputStream var1, OutputStream var2, int var3) throws IOException {
        byte var5 = -1;
        byte var6 = -1;
        byte var7 = -1;
        byte var8 = -1;
        if (var3 < 2) {
            SchedulerRuntimeException.logAndThrow("BASE64Decoder: Not enough bytes for an atom.");
        } else {
            int var4;
            do {
                var4 = var1.read();
                if (var4 == -1) {
                    throw new CEStreamExhausted();
                }
            } while(var4 == 10 || var4 == 13);

            this.decode_buffer[0] = (byte)var4;
            var4 = this.readFully(var1, this.decode_buffer, 1, var3 - 1);
            if (var4 == -1) {
                throw new CEStreamExhausted();
            } else {
                if (var3 > 3 && this.decode_buffer[3] == 61) {
                    var3 = 3;
                }

                if (var3 > 2 && this.decode_buffer[2] == 61) {
                    var3 = 2;
                }

                switch(var3) {
                    case 4:
                        var8 = pem_convert_array[this.decode_buffer[3] & 255];
                    case 3:
                        var7 = pem_convert_array[this.decode_buffer[2] & 255];
                    case 2:
                        var6 = pem_convert_array[this.decode_buffer[1] & 255];
                        var5 = pem_convert_array[this.decode_buffer[0] & 255];
                    default:
                        switch(var3) {
                            case 2:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                break;
                            case 3:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte)(var6 << 4 & 240 | var7 >>> 2 & 15));
                                break;
                            case 4:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte)(var6 << 4 & 240 | var7 >>> 2 & 15));
                                var2.write((byte)(var7 << 6 & 192 | var8 & 63));
                        }

                }
            }
        }
    }

    static {
        int var0;
        for(var0 = 0; var0 < 255; ++var0) {
            pem_convert_array[var0] = -1;
        }

        for(var0 = 0; var0 < pem_array.length; ++var0) {
            pem_convert_array[pem_array[var0]] = (byte)var0;
        }

    }

    public static byte[] base64DecodeOld(final String encryptedValue) {
        try {
            return (new BASE64Decoder()).decodeBuffer(encryptedValue);
        } catch (IOException e) {
            throw new RuntimeException("Base64 decoding failed", e);
        }
    }

}

