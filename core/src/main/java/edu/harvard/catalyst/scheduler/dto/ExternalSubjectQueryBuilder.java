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
package edu.harvard.catalyst.scheduler.dto;

import org.apache.commons.lang.StringUtils;

import static edu.harvard.catalyst.scheduler.util.MiscUtil.isNonNullNonEmpty;

/**
 * Created by marc-danie on 6/9/17.
 */
public class ExternalSubjectQueryBuilder {
    static final int MAX_RECORDS = 50;

    private String lastName;
    private String firstName;
    private String birthdate;
    private String genderCode;
    private String mrn;
    private String puid;
    private String mrnSite;
    private String puidSite;


    public ExternalSubjectQueryBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ExternalSubjectQueryBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ExternalSubjectQueryBuilder birthdate(String birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public ExternalSubjectQueryBuilder genderCode(String genderCode) {
        this.genderCode = genderCode;
        return this;
    }

    public ExternalSubjectQueryBuilder mrn(String mrn) {
        this.mrn = mrn;
        return this;
    }

    public ExternalSubjectQueryBuilder puid(String puid) {
        this.puid = puid;
        return this;
    }

    public ExternalSubjectQueryBuilder mrnSite(String mrnSite) {
        this.mrnSite = mrnSite;
        return this;
    }

    public ExternalSubjectQueryBuilder puidSite(String puidSite) {
        this.puidSite = puidSite;
        return this;
    }

    // name and/or mrn need to turn into the Search= parameter for empi
    public String nameAndMrnToEmpiSearchParameter(final String name, final String mrn) {
        String result = mrn;

        if ((result == null || result.isEmpty())
            && (isNonNullNonEmpty(name))) {

            result = name;
        }
        return result;
    }

    public String build() {

        final String subjectLastAndFirstName = lastName + ", " + firstName;

        StringBuilder queryBuilder = new StringBuilder("<?xml version=\"1.0\"?><Query ");

        String site;
        String subjectMrnOrPuId;
        if(puid != null) {
            subjectMrnOrPuId = puid;
            site = puidSite;
        }
        else {
            subjectMrnOrPuId = mrn;
            site = mrnSite;
        }

        queryBuilder.append("Search=\"").append(nameAndMrnToEmpiSearchParameter(subjectLastAndFirstName, subjectMrnOrPuId)).append("\"");

        if (StringUtils.isNotBlank(birthdate)) {
            queryBuilder.append(" Age=\"").append(birthdate).append("\"");
        }
        if (StringUtils.isNotBlank(genderCode)) {
            queryBuilder.append(" Sex=\"").append(genderCode).append("\"");
        }
        if (StringUtils.isNotBlank(site)) {
            queryBuilder.append(" Site=\"").append(site).append("\"");
        }

        queryBuilder.append(" Max=\"").append(MAX_RECORDS).append("\"");

        queryBuilder.append(" />");

        return queryBuilder.toString();
    }
}
