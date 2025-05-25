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
package edu.harvard.catalyst.scheduler.decrypter;

import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.util.BaseEnum;
import org.apache.commons.cli.*;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.Key;
import java.sql.*;
import java.util.List;

/**
 * @author Bill Simons
 * @date 7/21/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Component
public class SubjectDecrypter {

    private final SubjectDAO subjectDAO;
    private final DataSource dataSource;

    @Autowired
    public SubjectDecrypter(SubjectDAO subjectDAO,
                            DataSource dataSource,
                            @Qualifier(value = "encryptionKeySubjectDecrypter") Key key) {
        this.subjectDAO = subjectDAO;
        this.dataSource = dataSource;

        SubjectDataEncryptor.setEncryptionKey(key);
    }


    void run() {
        System.out.println("Running with subjectDAO " + subjectDAO);

        Connection connection = null;
        PreparedStatement statement = null;

        int numDecryptedSubjects = 0;
        int numDecryptedSubjectMrns = 0;

        try {
            connection = dataSource.getConnection();

            statement = connection.prepareStatement("truncate table decrypted_subject");
            statement.execute();

            String createDecryptedSubjectString =
                    "insert into decrypted_subject ( " +
                            "id, " +
                            "active, " +
                            "city, " +
                            "created_date, " +
                            "ethnicity, " +
                            "first_name, " +
                            "full_name, " +
                            "gender, " +
                            "gender_empi, " +
                            "last_name, " +
                            "middle_name, " +
                            "primary_contact_number, " +
                            "race, " +
                            "secondary_contact_number, " +
                            "secure, " +
                            "state, " +
                            "street_address1, " +
                            "street_address2, " +
                            "zip, " +
                            "birthdate, " +
                            "country, " +
                            "comment, " +
                            "archival_status, " +
                            "latest_ssot_refresh, " +
                            "puid, " +
                            "gender_enum )" +
                            " values ( " +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?" +
                            ")" +
                            "";

            statement = connection.prepareStatement(createDecryptedSubjectString);

            List<Subject> subjects = subjectDAO.findAllSubjectsHql();
            int numSubjects = subjects.size();
            System.out.println("There are " + numSubjects + " subjects to deal with");

            for (Subject subject : subjects) {
                Subject decryptedSubject = SubjectDataEncryptor.decryptSubject(subject);
                insertSubjectQuery(decryptedSubject, statement);

                numDecryptedSubjects++;
            }



            statement = connection.prepareStatement("truncate table decrypted_subject_mrn");
            statement.execute();

            String createDecryptedSubjectMrnString =
                    "insert into decrypted_subject_mrn ( " +
                         "id, " +
                         "subject, " +
                         "mrn, " +
                         "site, " +
                         "status" +
                            ")" +
                            " values ( " +
                            "?," +
                            "?," +
                            "?," +
                            "?," +
                            "?" +
                            ")" +
                            "";

            statement = connection.prepareStatement(createDecryptedSubjectMrnString);

            List<SubjectMrn> subjectMrns = subjectDAO.findAllSubjectMrns();
            int numSubjectMrns = subjectMrns.size();
            System.out.println("There are " + numSubjectMrns + " subjectMrns to deal with");

            for (SubjectMrn subjectMrn : subjectMrns) {
                insertSubjectMrnQuery(subjectMrn, statement);

                numDecryptedSubjectMrns++;
            }




        }
        catch (SQLException se) {
            System.out.println("SQL Failure! " + se.getMessage());
        }
        finally {
            System.out.println("Succeeded to decrypt and store " + numDecryptedSubjects + " subjects");
            System.out.println("Succeeded to decrypt and store " + numDecryptedSubjectMrns + " subjectMrns");
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                System.out.println("SQL Failure while trying to close up shop! " + se.getMessage());
            }
        }
    }

    void setPrimitiveIntOrNull(int position, Integer value, PreparedStatement statement) throws SQLException {
        if (value != null) {
            statement.setInt(position, value);
        }
        else {
            statement.setNull(position, Types.INTEGER);
        }
    }
    void setPrimitiveBoolOrNull(int position, Boolean value, PreparedStatement statement) throws SQLException {
        if (value != null) {
            statement.setBoolean(position, value);
        }
        else {
            statement.setNull(position, Types.BOOLEAN);
        }
    }

    Integer idOrNull(BaseEntity entity) {
        return entity == null ? null : entity.getId();
    }
    Timestamp timestampOrNull(java.util.Date date) {
        Timestamp result = date == null ? null : new Timestamp(date.getTime());
        return result;
    }
    String nameOrNull(BaseEnum input) {
        return input == null ? null : input.name();
    }
    
    void insertSubjectQuery(Subject entity, PreparedStatement statement) throws SQLException {

        Timestamp birthStamp =     timestampOrNull(entity.getBirthdate());
        Timestamp createStamp =    timestampOrNull(entity.getCreatedDate());
        Timestamp latest_refresh = timestampOrNull(entity.getLatestSsotRefresh());

        Integer ethnicity = idOrNull(entity.getEthnicity());
        Integer race =      idOrNull(entity.getRace());
        Integer gender =    idOrNull(entity.getGender());
        Integer state =     idOrNull(entity.getState());
        Integer country =   idOrNull(entity.getCountry());

        String archivalStatus = nameOrNull(entity.getArchivalStatus());
        String genderType =     nameOrNull(entity.getGenderType());

        int index = 0;
        setPrimitiveIntOrNull( ++index, entity.getId(), statement); // but anyway, DB won't allow null
        setPrimitiveBoolOrNull(++index, entity.getActive(), statement);
        statement.setString   (++index, entity.getCity());
        statement.setTimestamp(++index, createStamp);
        setPrimitiveIntOrNull (++index, ethnicity, statement);
        statement.setString(   ++index, entity.getFirstName());
        statement.setString   (++index, entity.getFullName());
        setPrimitiveIntOrNull (++index, gender, statement);
        statement.setString  ( ++index, entity.getGenderEmpi());
        statement.setString(   ++index, entity.getLastName());
        statement.setString(   ++index, entity.getMiddleName());
        statement.setString   (++index, entity.getPrimaryContactNumber());
        setPrimitiveIntOrNull( ++index, race, statement);
        statement.setString   (++index, entity.getSecondaryContactNumber());
        setPrimitiveBoolOrNull(++index, entity.getSecure(), statement); // but anyway, DB won't allow null
        setPrimitiveIntOrNull (++index, state, statement);
        statement.setString(   ++index, entity.getStreetAddress1());
        statement.setString(   ++index, entity.getStreetAddress2());
        statement.setString   (++index, entity.getZip());
        statement.setTimestamp(++index, birthStamp);
        setPrimitiveIntOrNull( ++index, country, statement);
        statement.setString(   ++index, entity.getComment());
        statement.setString(   ++index, archivalStatus);
        statement.setTimestamp(++index, latest_refresh);
        statement.setString(   ++index, entity.getPuid());
        statement.setString(   ++index, genderType);

        statement.executeUpdate();

    }
    void insertSubjectMrnQuery(SubjectMrn entity, PreparedStatement statement) throws SQLException {

        int index = 0;
        setPrimitiveIntOrNull( ++index, entity.getId(), statement); // but anyway, DB won't allow null
        setPrimitiveIntOrNull( ++index, entity.getSubject().getId(), statement); // but anyway, DB won't allow null
        statement.setString(   ++index, SubjectDataEncryptor.decrypt(entity.getMrn()));
        statement.setString(   ++index, entity.getSite());
        statement.setString(   ++index, entity.getStatus());

        statement.executeUpdate();

    }
    private static Options constructOptions() {
        Options options = new Options();
        options.addOption("help", false, "display this message");
        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("decrypter", options);
    }
    public static void main(String[] args) throws ParseException {
        Options options = constructOptions();

        CommandLineParser parser = new BasicParser();

        CommandLine line = parser.parse(options, args);
        if(line.hasOption("help")) {
            printHelp(options);
            System.exit(1);
        }
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-subject-decrypter.xml");

        SubjectDecrypter decrypter = (SubjectDecrypter) context.getBean("subjectDecrypter");

        decrypter.run();

    }
}
