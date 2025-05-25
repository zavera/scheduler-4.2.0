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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 5/19/15
 * Time: 3:42 PM
 */
@Repository
public class EpicDataMappingDAO extends SubjectDAO {

    private static final Logger LOG = Logger.getLogger(EpicDataMappingDAO.class);

    static int UNREPORTED_INT = 5;

    int empiGenderToGenderId(String empiGender) {
        int result = UNREPORTED_INT;

        switch (empiGender) {
            case "M":
                result = 1;
                break;
            case "F":
                result = 2;
                break;
        }
        return result;
    }

    String empiGenderToGenderName(String empiGender) {
        int genderId = empiGenderToGenderId(empiGender);

        String name = super.findFieldById("Gender", genderId, "name");

        return name;
    }
}
