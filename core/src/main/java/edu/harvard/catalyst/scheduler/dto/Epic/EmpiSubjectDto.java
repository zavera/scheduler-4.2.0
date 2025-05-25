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
package edu.harvard.catalyst.scheduler.dto.Epic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: carl
 * Date: 6/3/15
 * Time: 2:03 PM
 */

@XmlRootElement(name="QueryReply")
@XmlAccessorType(XmlAccessType.FIELD)

public class EmpiSubjectDto {
    private String logMessage;

    @XmlAttribute(name = "Session")
    private int session;

    @XmlElement(name = "Patients")
    private Patients patients;

    public EmpiSubjectDto() {}

    public Patients getPatients() {
        return patients;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public EmpiSubjectDto(List<Patient> patients) {
        this.patients = new Patients(patients);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Patients {
        @XmlElement(name = "Patient")
        private List<Patient> patientList = new ArrayList<>();

        public Patients() {}

        public List<Patient> getPatientList() {
            return patientList;
        }

        public Patients(List<Patient> patientList) {
            this.patientList = patientList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Patient {
        @XmlAttribute(name = "UID")
        private int uid;

        @XmlAttribute(name = "Status")
        private String status;

        @XmlAttribute(name = "Gender")
        private String gender;

        @XmlAttribute(name = "DOB")
        private String dobString;

        @XmlAttribute(name = "SSN")
        private String ssn;

        @XmlAttribute(name = "VIP")
        private String vip;

        @XmlAttribute(name = "TestFlag")
        private String testFlag;

        @XmlElement(name = "MRNs")
        private Mrns mrns;

        @XmlElement(name = "Name")
        private Name name;

        @XmlElement(name = "Address")
        private Address address;

        @XmlElement(name = "Phones")
        private Phones phones;

        @XmlElement(name = "Aliases")
        private Aliases aliases;

        @XmlElement(name = "LastFiled")
        private LastFiled lastFiled;

        @XmlElement(name = "OtherPID")
        private OtherPid otherPid;

        @XmlElement(name = "SCFlags")
        private SCFlags scFlags;

        @XmlElement(name = "Ethnicity")
        private Ethnicity ethnicity;

        @XmlElement(name = "DupeMRNs")
        private DupMrns dupMrns;

        public Patient() {}
        public int getUid() {
            return uid;
        }

        public String getStatus() {
            return status;
        }

        public String getGender() {
            return gender;
        }

        public String getDobString() {
            return dobString;
        }

        public Mrns getMrns() {
            return mrns;
        }

        public Name getName() {
            return name;
        }

        public Ethnicity getEthnicity() {
            return ethnicity;
        }

        public Address getAddress() {
            return address;
        }

        public Phones getPhones() {
            return phones;
        }

        public Aliases getAliases() {
            return aliases;
        }

        public LastFiled getLastFiled() {
            return lastFiled;
        }

        public OtherPid getOtherPid() {
            return otherPid;
        }

        public DupMrns getDupMrns() {
            return dupMrns;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public void setDobString(String dobString) {
            this.dobString = dobString;
        }

        public void setMrns(Mrns mrns) {
            this.mrns = mrns;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public void setPhones(Phones phones) {
            this.phones = phones;
        }

        public void setAliases(Aliases aliases) {
            this.aliases = aliases;
        }

        public void setLastFiled(LastFiled lastFiled) {
            this.lastFiled = lastFiled;
        }

        public void setOtherPid(OtherPid otherPid) {
            this.otherPid = otherPid;
        }

        public void setEthnicity(Ethnicity ethnicity) {
            this.ethnicity = ethnicity;
        }

        public void setDupMrns(DupMrns dupMrns) {
            this.dupMrns = dupMrns;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class SCFlags {
        @XmlElement(name = "SCFlag")
        private List<SCFlag> scFlagList;

        public SCFlags() {
            this.scFlagList = Lists.newArrayList();
        }

        public List<SCFlag> getScFlagList() {
            return scFlagList;
        }

        public void setScFlagList(List<SCFlag> scFlagList) {
            this.scFlagList = scFlagList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class SCFlag {
        @XmlAttribute(name = "Code")
        private String code;

        @XmlAttribute(name = "Status")
        private String status;

        public SCFlag() {}

        public SCFlag(String code, String status) {
            this.code = code;
            this.status = status;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Mrns {
        @XmlElement(name = "MRN")
        private List<Mrn> mrnList;

        public Mrns() {
            this.mrnList = Lists.newArrayList();
        }

        public List<Mrn> getMrnList() {
            return mrnList;
        }

        public void setMrnList(List<Mrn> mrnList) {
            this.mrnList = mrnList;
        }

        public String toCommaList() {
            String result = Joiner.on(",").join(mrnList.stream()
                    .map(m -> m.getValue() + ":" +
                            m.getSite() + ":" +
                            m.getStatus())
                    .collect(Collectors.toList()));

            return result;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Mrn {
        @XmlAttribute(name = "Site")
        private String site;

        @XmlAttribute(name = "Value")
        private String value;

        @XmlAttribute(name = "Status")
        private String status;

        public Mrn() {}

        public Mrn(String site, String value, String status) {
            this.site = site;
            this.value = value;
            this.status = status;
        }

        public String getSite() {
            return site;
        }

        public String getStatus() {
            return status;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Site: " + site + ", Value: " + value + ", Status: " + status;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Ethnicity {
        @XmlAttribute(name = "Ethnic1")
        private String ethnic1;

        @XmlAttribute(name = "Ethnic2")
        private String ethnic2;

        @XmlAttribute(name = "EthnicText")
        private String ethnicText;

        public Ethnicity() {}

        public Ethnicity(String ethnic1, String ethnic2, String ethnicText) {
            this.ethnic1 = ethnic1;
            this.ethnic2 = ethnic2;
            this.ethnicText = ethnicText;
        }

        public String getEthnic1() {
            return ethnic1;
        }

        public String getEthnic2() {
            return ethnic2;
        }

        public String getEthnicText() {
            return ethnicText;
        }

        public void setEthnic1(String ethnic1) {
            this.ethnic1 = ethnic1;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Name {
        @XmlAttribute(name = "Last")
        private String last;

        @XmlAttribute(name = "First")
        private String first;

        @XmlAttribute(name = "MI")
        private String middleInitial;

        @XmlAttribute(name = "Suffix")
        private String suffix;

        @XmlAttribute(name = "Prefix")
        private String prefix;

        public Name() {}

        public Name(String last, String first, String middleInitial) {
            this.last = last;
            this.first = first;
            this.middleInitial = middleInitial;
        }

        public String getLast() {
            return last;
        }

        public String getFirst() {
            return first;
        }
        public String getMiddleInitial() {
            return middleInitial;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String toCommentString() {
            return last + ", " + first + ", " + middleInitial;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Address {
        @XmlAttribute(name = "Line1")
        private String line1;

        @XmlAttribute(name = "Line2")
        private String line2;

        @XmlAttribute(name = "City")
        private String city;

        @XmlAttribute(name = "State")
        private String state;

        @XmlAttribute(name = "Zip")
        private String zipAsString;

        @XmlAttribute(name = "Country")
        private String country;

        public Address() {}

        public Address(String line1,
                       String line2,
                       String city,
                       String state,
                       String zipAsString,
                       String country) {
            this.line1 = line1;
            this.line2 = line2;
            this.city = city;
            this.state = state;
            this.zipAsString = zipAsString;
            this.country = country;
        }

        public String getLine1() {
            return line1;
        }

        public String getLine2() {
            return line2;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getZipAsString() {
            return zipAsString;
        }

        public String getCountry() {
            return country;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Phones {
        @XmlElement(name = "Phone")
        private List<Phone> phoneList;

        public Phones() {}

        public Phones(List<Phone> phoneList) {
            this.phoneList = phoneList;
        }

        public List<Phone> getPhoneList() {
            return phoneList;
        }

        public void setPhoneList(List<Phone> phoneList) {
            this.phoneList = phoneList;
        }

        public String toCommaList() {
            String result = Joiner.on(",").join(phoneList.stream()
                    .map(m -> m.getType() + ":" +
                            m.getNumber())
                    .collect(Collectors.toList()));

            return result;
        }

        public String getByPosition(int position) {
            Phone resultPhone;

            if (phoneList == null || phoneList.size() < position + 1) {
                resultPhone = null;
            }
            else {
                resultPhone = phoneList.get(position);
            }
            String result = (resultPhone == null ? null : resultPhone.getNumber());
            return result;
        }
        public String getPrimary() {
            return getByPosition(0);
        }
        public String getSecondary() {
            return getByPosition(1);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Phone {
        @XmlAttribute(name = "Type")
        private String type;

        @XmlAttribute(name = "Number")
        private String number;

        public Phone() {}

        public Phone(String type, String number) {
            this.type = type;
            this.number = number;
        }

        public String getType() {
            return type;
        }

        public String getNumber() {
            return number;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Aliases {
        @XmlElement(name = "Name")
        private List<Name> nameList;

        public Aliases() {
            this.nameList = Lists.newArrayList();
        }

        public Aliases(List<Name> nameList) {
            this.nameList = nameList;
        }

        public List<Name> getNameList() {
            return nameList;
        }

        public void setNameList(List<Name> nameList) {
            this.nameList = nameList;
        }

        public String toCommaList() {
            String result = Joiner.on(",").join(nameList.stream()
                    .map(m -> m.getLast() + ":" +
                            m.getFirst() + ":" +
                            m.getMiddleInitial())
                    .collect(Collectors.toList()));

            return result;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class LastFiled {
        @XmlAttribute(name = "Date")
        private String dateAsString;

        @XmlAttribute(name = "Site")
        private String site;

        public LastFiled() {}

        public LastFiled(String dateAsString, String site) {
            this.dateAsString = dateAsString;
            this.site = site;
        }

        public String getDateAsString() {
            return dateAsString;
        }

        public String getSite() {
            return site;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class OtherPid {
        @XmlAttribute(name = "Religion")
        private String religion;

        @XmlAttribute(name = "Language")
        private String language;

        @XmlAttribute(name = "Marital")
        private String marital;

        @XmlAttribute(name = "Vet")
        private String vet;

        @XmlAttribute(name = "Race")
        private String race;

        @XmlAttribute(name = "Mmaiden")
        private String nMaiden;

        @XmlAttribute(name = "Race2")
        private String race2;

        @XmlAttribute(name = "RaceText")
        private String raceText;

        public OtherPid() {}

        public OtherPid(String religion,
                        String marital,
                        String language,
                        String vet,
                        String race,
                        String nMaiden,
                        String race2,
                        String raceText) {
            this.religion = religion;
            this.marital = marital;
            this.language = language;
            this.vet = vet;
            this.race = race;
            this.nMaiden = nMaiden;
            this.race2 = race2;
            this.raceText = raceText;
        }

        public String getReligion() {
            return religion;
        }

        public String getMarital() {
            return marital;
        }

        public String getLanguage() {
            return language;
        }

        public String getVet() {
            return vet;
        }

        public String getRace() {
            return race;
        }

        public String getnMaiden() {
            return nMaiden;
        }

        public String getRace2() {
            return race2;
        }

        public String getRaceText() {
            return raceText;
        }

        public void setRace(String race) {
            this.race = race;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class DupMrns {
        @XmlElement(name = "DupeMRN")
        private List<DupMrn> dupMrnList;

        public DupMrns() {}

        public DupMrns(List<DupMrn> dupMrnList) {
            this.dupMrnList = dupMrnList;
        }

        public List<DupMrn> getDupMrnList() {
            return dupMrnList;
        }

        public String toCommaList() {
            String result = Joiner.on(",").join(dupMrnList.stream()
                    .map(m -> m.getValue() + ":" +
                            m.getSite())
                    .collect(Collectors.toList()));

            return result;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static public class DupMrn {
        @XmlAttribute(name = "Site")
        private String site;

        @XmlAttribute(name = "Value")
        private String value;

        public DupMrn() {}

        public DupMrn(String site, String value) {
            this.site = site;
            this.value = value;
        }

        public String getSite() {
            return site;
        }

        public String getValue() {
            return value;
        }
    }

    //////////////////////////////
    static public EmpiSubjectDto unmarshall(String xmlInput) {
        EmpiSubjectDto empiSubjectDto = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EmpiSubjectDto.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputStream stringAsStream = new ByteArrayInputStream(xmlInput.getBytes(StandardCharsets.UTF_8));
            empiSubjectDto = (EmpiSubjectDto) jaxbUnmarshaller.unmarshal(stringAsStream);

        } catch (JAXBException e) {
            SchedulerRuntimeException.logAndThrow("Cannot unmarshall the string: " + xmlInput, e);
        }

        return empiSubjectDto;
    }

    public String marshall() {
        String result = null;

        try {
            StringWriter stringWriter = new StringWriter();

            JAXBContext context = JAXBContext.newInstance(EmpiSubjectDto.class);
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(this, stringWriter);
            result = stringWriter.toString();

        } catch (JAXBException e) {
            SchedulerRuntimeException.logAndThrow("Cannot marshall the EmpiSubjectDto", e);

        }

        return result;
    }

}

