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
import edu.harvard.catalyst.scheduler.entity.StudySubject;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

/**
 * @author smitha
 */
public final class SubjectDataEncryptor {

    private Key encryptionKey;

    public static void setEncryptionKey(final Key encryptionKey) {
        getInstance().encryptionKey = encryptionKey;
    }

    //NB: Holder idiom for efficient thread-safe lazy init
    private static final class Holder {
        private static final SubjectDataEncryptor instance = new SubjectDataEncryptor();
    }

    private static SubjectDataEncryptor getInstance() {
        return Holder.instance;
    }

    private SubjectDataEncryptor() {
    }

    static final String ALGORITHM = "AES";

    // a helper for encrypting a string after converting it to uppercase, useful
    // as a lambda expression to operate on a stream
    public static final Function<String, String> capitalizeAndEncrypt = value ->
            SubjectDataEncryptor.encrypt(value.toUpperCase());

    public static String encrypt(final String valueToEnc) {
        String encryptedValue = null;

        final String complaint = "Couldn't encrypt '" + valueToEnc + "'";
        try {
            if (valueToEnc != null) {

                // the following doesn't seem to do anything.
                getInstance();
                final Key key = getKey();
                final Cipher c = Cipher.getInstance(ALGORITHM);
                c.init(Cipher.ENCRYPT_MODE, key);
                final byte[] encValue = c.doFinal(valueToEnc.getBytes());

                encryptedValue = Base64.base64Encode(encValue);

            } else {
                return "";
            }
        } catch (final NoSuchPaddingException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final NoSuchAlgorithmException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final InvalidKeyException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final BadPaddingException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final IllegalBlockSizeException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        }

        return encryptedValue;
    }

    public static String decrypt(final String encryptedValue) {
        return decrypt(encryptedValue, getKey());
    }

    public static String decrypt(final String encryptedValue, final Key key) {
        String decryptedValue = null;

        final String complaint = "Couldn't decrypt '" + encryptedValue + "'";
        try {
            if (encryptedValue != null) {

                final Cipher c = Cipher.getInstance(ALGORITHM);
                c.init(Cipher.DECRYPT_MODE, key);

                final byte[] decordedValue = Base64.base64Decode(encryptedValue);
                final byte[] decValue = c.doFinal(decordedValue);

                decryptedValue = new String(decValue);

            } else {
                return null;
            }
        } catch (final NoSuchPaddingException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final NoSuchAlgorithmException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final InvalidKeyException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final BadPaddingException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        } catch (final IllegalBlockSizeException exception) {
            SchedulerRuntimeException.logAndThrow(complaint, exception);
        }

        return decryptedValue;
    }

    static Key getKey() {
        return getInstance().encryptionKey;
    }

    /**
     * This method turns the strings to uppercase in addition to encrypting them
     * @param subjectEntity
     * @return
     */
    public static Subject encryptSubjectInPlace(final Subject subjectEntity) {
        if (subjectEntity != null) {
            // encrypt it

            if (subjectEntity.getFirstName() != null) {
                subjectEntity.setFirstName(encrypt(subjectEntity.getFirstName().toUpperCase()));
            }

            if (subjectEntity.getMiddleName() != null) {
                subjectEntity.setMiddleName(encrypt(subjectEntity.getMiddleName().toUpperCase()));
            }

            if (subjectEntity.getPuid() != null) {
                subjectEntity.setPuid(encrypt(subjectEntity.getPuid().toUpperCase()));
            }

            if (subjectEntity.getLastName() != null) {
                subjectEntity.setLastName(encrypt(subjectEntity.getLastName().toUpperCase()));
            }

            if (subjectEntity.getFullName() != null) {
                subjectEntity.setFullName(encrypt(subjectEntity.getFullName().toUpperCase()));
            }

            if (subjectEntity.getStreetAddress1() != null) {
                subjectEntity.setStreetAddress1(encrypt(subjectEntity.getStreetAddress1().toUpperCase()));
            }

            if (subjectEntity.getStreetAddress2() != null) {
                subjectEntity.setStreetAddress2(encrypt(subjectEntity.getStreetAddress2().toUpperCase()));
            }

            if (subjectEntity.getCity() != null) {
                subjectEntity.setCity(encrypt(subjectEntity.getCity().toUpperCase()));
            }

            if (subjectEntity.getZip() != null) {
                subjectEntity.setZip(encrypt(subjectEntity.getZip().toUpperCase()));
            }

            if (subjectEntity.getPrimaryContactNumber() != null) {
                subjectEntity.setPrimaryContactNumber(encrypt(subjectEntity.getPrimaryContactNumber().toUpperCase()));
            }

            if (subjectEntity.getSecondaryContactNumber() != null) {
                subjectEntity.setSecondaryContactNumber(encrypt(subjectEntity.getSecondaryContactNumber().toUpperCase()));
            }
            
            subjectEntity.setSecure(Boolean.TRUE);
        }
        return subjectEntity;
    }

    /**
     * This is subtly different from decryptSubjectMrnAndSubject; it doesn't decrypt the SubjectMrn value
     *
     * TODO: (technical debt) address later if we should decrypt the mrn as well;
     * in which case we can call decryptSubjectMrnAndSubject from within this method and reduce code duplication
     */
    public static void decryptSubjectWithinStudySubject(final StudySubject studySubject) {
        if (studySubject == null) {
            return;
        }

        final Subject encrypted = studySubject.getSubject();
        final Subject subjectCopy = Subject.defensiveCopy(encrypted);
        final Subject decrypted = decryptSubject(subjectCopy);

        SubjectMrn subjectMrnCopy = SubjectMrn.defensiveCopy(studySubject.getSubjectMrn());
        subjectMrnCopy.setSubject(decrypted);
        studySubject.setSubjectMrn(subjectMrnCopy);
    }

    public static SubjectMrn decryptSubjectMrnAndSubject(final SubjectMrn subjectMrn) {
        if (subjectMrn == null) {
            return null;
        }

        final SubjectMrn subjectMrnCopy = SubjectMrn.defensiveCopy(subjectMrn);
        final Subject encryptedSubject = subjectMrnCopy.getSubject();
        final Subject decryptedSubject = decryptSubject(encryptedSubject);

        final String decryptedMrn = decrypt(subjectMrnCopy.getMrn());

        subjectMrnCopy.setMrn(decryptedMrn);
        subjectMrnCopy.setSubject(decryptedSubject);

        return subjectMrnCopy;
    }

    public static Subject decryptSubject(final Subject subjectEntity) {
        return decryptSubject(subjectEntity, Subject.defensiveCopy(subjectEntity), getKey());
    }

    public static Subject decryptSubjectMrnWithinSubject(final Subject subjectEntity) {

        final Subject targetSubject = Subject.defensiveCopy(subjectEntity);

        if (subjectEntity == null) {
            return null;
        }

        if (targetSubject.getSecure()) {

            targetSubject.setSubjectMrnSet(targetSubject.getDecryptedSubjectMrnSet());
            targetSubject.setSecure(false);
        }
        return targetSubject;
    }

    public static Subject decryptSubjectLastName(final Subject subjectEntity) {

        final Subject targetSubject = Subject.defensiveCopy(subjectEntity);
        final Key key = getKey();

        if (subjectEntity == null) {
            return null;
        }

        if (targetSubject.getSecure()) {

            targetSubject.setLastName(decrypt(targetSubject.getLastName(), key));
            targetSubject.setSecure(false);
        }
        return targetSubject;
    }

    public static Subject decryptSubjectInPlace(final Subject subjectEntity) {
        return decryptSubject(subjectEntity, subjectEntity, getKey());
    }

    public static Subject decryptSubject(final Subject startingSubject, final Subject targetSubject, final Key key) {
        if (startingSubject == null) {
            return null;
        }

        if (targetSubject.getSecure()) {

            targetSubject.setFullName(decrypt(targetSubject.getFullName(), key));
            targetSubject.setFirstName(decrypt(targetSubject.getFirstName(), key));
            targetSubject.setPuid(decrypt(targetSubject.getPuid(), key));
            targetSubject.setLastName(decrypt(targetSubject.getLastName(), key));
            targetSubject.setMiddleName(decrypt(targetSubject.getMiddleName(), key));
            targetSubject.setStreetAddress1(decrypt(targetSubject.getStreetAddress1(), key));
            targetSubject.setStreetAddress2(decrypt(targetSubject.getStreetAddress2(), key));
            targetSubject.setCity(decrypt(targetSubject.getCity(), key));
            targetSubject.setZip(decrypt(targetSubject.getZip(), key));
            targetSubject.setPrimaryContactNumber(decrypt(targetSubject.getPrimaryContactNumber(), key));
            targetSubject.setSecondaryContactNumber(decrypt(targetSubject.getSecondaryContactNumber(), key));
            targetSubject.setSubjectMrnSet(targetSubject.getDecryptedSubjectMrnSet());

            targetSubject.setSecure(false);
        }
        return targetSubject;
    }
}
