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
package edu.harvard.catalyst.scheduler.fakeSubjects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.harvard.catalyst.scheduler.entity.GenderType;
import edu.harvard.catalyst.scheduler.util.DateUtility;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.Key;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Bill Simons
 * @date 7/21/14
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 */
@Component
public class FakeSubjects {

    private static Map<Integer, String> stateIdToCityMap;
    private static List<Integer> ethnicityIds;
    private static List<Integer> raceIds;
    private static List<Integer> genderIds;
    private final static int inverseFrequencyOfMrnMutations = 20;

    private static String fakeSite = "nss";

    static {
        stateIdToCityMap = generateStateIdToCityMap();

        ethnicityIds = Lists.newArrayList(1, 2, 3, 4);

        raceIds = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);

        genderIds = Lists.newArrayList(1, 2, 3, 4, 5, 6);
    }

    private static final Logger log = Logger.getLogger(FakeSubjects.class);

    private static Map<Integer, String> generateStateIdToCityMap() {
        Map<Integer, String> mapToPopulate = Maps.newHashMap();
        mapToPopulate.put(1, "Boston");
        mapToPopulate.put(43, "Austin");
        mapToPopulate.put(32, "New York City");
        mapToPopulate.put(14, "Chicago");
        mapToPopulate.put(6, "Los Angeles");
        mapToPopulate.put(43, "Houston");
        mapToPopulate.put(47, "Seattle");
        mapToPopulate.put(11, "Atlanta");
        return mapToPopulate;
    }

    private final DataSource dataSource;

    @Autowired
    public FakeSubjects(DataSource dataSource,
                        @Qualifier(value = "encryptionKeyFake") Key key) {

        this.dataSource = dataSource;

        SubjectDataEncryptor.setEncryptionKey(key);
    }

    static long startTime;
    static long endTime;

    public static void main(String[] args) throws SQLException {

        java.util.Date startDate = new java.util.Date();
        startTime = startDate.getTime();
        System.out.println("START Fake Subjects Populator at " + startDate.toString());


        // pass in number of desired fake subjects
        Integer numSubjects = Integer.valueOf(args[0]);
        if (args.length > 1) {
            fakeSite = args[1];
        }

        boolean resetId = true;
        if (args.length > 2 && args[2].equals("--noResetId")) {
            resetId = false;
        }
        log.info("reset autoincrement id column: " + resetId);

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-populate-fake-subjects.xml");

        FakeSubjects fakeSubjects = (FakeSubjects) context.getBean("fakeSubjects");

        fakeSubjects.runSubjects(numSubjects, resetId);
        fakeSubjects.runSubjectMrns(numSubjects);

        java.util.Date endDate = new java.util.Date();
        endTime = endDate.getTime();

        long elapsedTime = endTime - startTime;

        System.out.println("Elapsed time: " + elapsedTime / 1000 + " seconds");
        System.out.println("END Fake Subjects Populator at " + endDate.toString());

    }

    void runSubjectMrns(int numSubjects) throws SQLException {

        int numFaked = 0;
        int numPrevious = 0;
        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();
            statement.executeQuery("set FOREIGN_KEY_CHECKS=0");
            statement.executeQuery("SET AUTOCOMMIT = 0");

            ResultSet resultSet = statement.executeQuery("select count(*) from subject_mrn");
            resultSet.first();
            numPrevious = resultSet.getInt(1);

            if (numPrevious == 0) { // generate one for each subject
              statement.executeUpdate("ALTER TABLE `subject_mrn` AUTO_INCREMENT = 1;");

              String createSubjectMrnString =
                  "insert into subject_mrn ( " +
                      "subject, " +
                      "mrn, " +
                      "site, " +
                      "status " +
                      ") " +
                      " values ( " +
                      "?," +
                      "?," +
                      "?," +
                      "?" +
                      ")" +
                      "";
              PreparedStatement createSubjectMrnStatement = connection.prepareStatement(createSubjectMrnString);
              for (int i = 1; i <= numSubjects; i++) {

                int paramIndex = 0;
                String encryptedMrn = SubjectDataEncryptor.encrypt(Integer.toString(888000 + i));
                createSubjectMrnStatement.setInt(++paramIndex, i);
                createSubjectMrnStatement.setString(++paramIndex, encryptedMrn);
                createSubjectMrnStatement.setString(++paramIndex, fakeSite);
                createSubjectMrnStatement.setString(++paramIndex, null);
                createSubjectMrnStatement.executeUpdate();

                numFaked++;
              }
            }
            else {
                String updateSubjectMrnString =
                    "update subject_mrn set " +
                        "mrn=?, " +
                        "site=? " +
                        "where id=?";

                PreparedStatement updateSubjectMrnStatement = connection.prepareStatement(updateSubjectMrnString);

                resultSet = statement.executeQuery("select id from subject_mrn");

                while (resultSet != null && resultSet.next()) {
                    int id = resultSet.getInt(1);

                    updateSubjectMrnQuery(updateSubjectMrnStatement, id, numSubjects);

                    numFaked++;
                }

            }
            statement.executeQuery("SET AUTOCOMMIT = 1");
            statement.executeQuery("set FOREIGN_KEY_CHECKS=1");
        } finally {
            String message = "Succeeded to update/insert " + numFaked + " SubjectMrns, based on " +
                numPrevious + " previous records and " +
                numSubjects + " subjects";

            System.out.println(message);
            log.info(message);
        }
    }

    void updateSubjectMrnQuery(PreparedStatement updateSubjectMrnStatement,
                               int id,
                               int numSubjects) throws SQLException {

        String encryptedMrn = SubjectDataEncryptor.encrypt(Integer.toString(888000 + id));

        int numIdOverSubject = id / numSubjects;
        String fakeSuffix = numIdOverSubject == 0 ? "" : Integer.toString(numIdOverSubject);
        String suffixedSite = fakeSite + fakeSuffix;

        updateSubjectMrnStatement.setString(1, encryptedMrn);
        updateSubjectMrnStatement.setString(2, suffixedSite);
        updateSubjectMrnStatement.setInt(3, id);

        updateSubjectMrnStatement.executeUpdate();
    }

    void runSubjects(int numSubjects, boolean resetId) throws SQLException {

        int numFaked = 0;
        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();
            statement.executeQuery("SET AUTOCOMMIT = 0");

            if (resetId) {
                // reset subject table id column
                statement.executeUpdate("ALTER TABLE `subject` AUTO_INCREMENT = 1;");
            }

            String createSubjectString =
                    "insert into subject ( " +
                    "first_name, " +
                    "middle_name, " +
                    "last_name, " +
                    "full_name, " +
                    "ethnicity, " +
                    "birthdate, " +
                    "race, " +
                    "gender, " +
                    "gender_empi, " +
                    "gender_enum, " +
                    "puid, " +
                    "latest_ssot_refresh, " +
                    "archival_status, " +
                    "street_address1, " +
                    "street_address2, " +
                    "city, " +
                    "state, " +
                    "zip, " +
                    "comment, " +
                    "country, " +
                    "primary_contact_number, " +
                    "secondary_contact_number, " +
                    "created_date, " +
                    "secure, " +
                    "active " +
                    ") " +
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
                    "?" +
                    ")" +
                    "";

            PreparedStatement createSubjectStatement = connection.prepareStatement(createSubjectString);

            for (int i = 1; i <= numSubjects; i++) {

                insertSubjectQuery(createSubjectStatement, /* for 3.0.0 createSubjectMrnStatement,*/ i);

                numFaked++;
            }

            statement.executeQuery("SET AUTOCOMMIT = 1");

        }
        finally {
            String message = "Succeeded to create " + numFaked + " fake Subjects, out of " + numSubjects + " desired";

            System.out.println(message);
            log.info(message);
        }
    }

    private Date randomDateOfBirth() {
        Calendar gc = Calendar.getInstance();
        gc.add(Calendar.YEAR, -1);
        int year = randBetween(1900, gc.get(Calendar.YEAR));
        gc.set(gc.YEAR, year);
        int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
        gc.set(gc.DAY_OF_YEAR, dayOfYear);
        return new Date(gc.getTimeInMillis());
    }

    private String randomMrn(Random random, int index) {

        // TODO-XH : extract the numbers into constant(s)
        boolean hasLeadingSpace = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;
        boolean hasMiddleSpace = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;
        boolean hasTrailingSpace = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;
        boolean hasLeadingDash = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;
        boolean hasMiddleDash = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;
        boolean hasTrailingDash = random.nextInt(inverseFrequencyOfMrnMutations) ==  0;

        StringBuffer buffer = new StringBuffer();

        if (hasLeadingSpace) {
            buffer.append(" ");
        }
        if (hasLeadingDash) {
            buffer.append("-");
        }

        buffer.append(index);

        if (hasMiddleSpace) {
            buffer.append(" ");
        }
        if (hasMiddleDash) {
            buffer.append("-");
        }
        buffer.append(index);
        if (hasTrailingSpace) {
            buffer.append(" ");
        }
        if (hasTrailingDash) {
            buffer.append("-");
        }

        return buffer.toString();

    }

    public int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }


    void insertSubjectQuery(PreparedStatement createSubjectStatement,
                            int index) throws SQLException {
        Random random = new Random();
        List<Integer> keys = Lists.newArrayList(stateIdToCityMap.keySet());
        Integer stateId   = keys.get(random.nextInt(keys.size()));
        String cityName   = stateIdToCityMap.get(stateId).toUpperCase();

        Date birthDate = randomDateOfBirth();
        Date ssot_refresh = new Date((DateUtility.toDate(LocalDateTime.now())).getTime());
        Timestamp createStamp = new Timestamp(Calendar.getInstance().getTimeInMillis());

        Integer ethnicity = ethnicityIds.get(random.nextInt(ethnicityIds.size()));
        Integer race = raceIds.get(random.nextInt(raceIds.size()));
        Integer gender = genderIds.get(random.nextInt(genderIds.size()));
        Integer state = Integer.valueOf(stateId);
        Integer country = Integer.valueOf(1);

        String city = SubjectDataEncryptor.encrypt(cityName);
        String middleName = SubjectDataEncryptor.encrypt("");
        String primaryContactNumber = SubjectDataEncryptor.encrypt("555-111-1111");
        String secondaryContactNumber = SubjectDataEncryptor.encrypt("555-111-1111");
        String streetAddress1 = SubjectDataEncryptor.encrypt("123 Main St");
        String streetAddress2 = SubjectDataEncryptor.encrypt("");
        String zip = SubjectDataEncryptor.encrypt("01234");

        String first = "TEST";
        String last = "SUBJECT"+index;
        String full = first + " " + last;

        String firstName = SubjectDataEncryptor.encrypt(first);
        String lastName = SubjectDataEncryptor.encrypt(last);
        String fullName = SubjectDataEncryptor.encrypt(full);

        String puid = SubjectDataEncryptor.encrypt(Integer.toString(888000 + index));

        int index2=0;
        createSubjectStatement.setString   (++index2, firstName);
        createSubjectStatement.setString   (++index2, middleName);
        createSubjectStatement.setString   (++index2, lastName);
        createSubjectStatement.setString   (++index2, fullName);
        createSubjectStatement.setInt      (++index2, ethnicity);
        createSubjectStatement.setDate     (++index2, birthDate);
        createSubjectStatement.setInt      (++index2, race);
        createSubjectStatement.setInt      (++index2, gender);

        createSubjectStatement.setString   (++index2, "U");
        createSubjectStatement.setString   (++index2, GenderType.UNREPORTED.name());
        createSubjectStatement.setString   (++index2, puid);
        createSubjectStatement.setDate     (++index2, ssot_refresh);
        createSubjectStatement.setNull     (++index2, Types.VARCHAR);

        createSubjectStatement.setString   (++index2, streetAddress1);
        createSubjectStatement.setString   (++index2, streetAddress2);
        createSubjectStatement.setString   (++index2, city);
        createSubjectStatement.setInt      (++index2, state);
        createSubjectStatement.setString   (++index2, zip);
        createSubjectStatement.setString   (++index2, "");
        createSubjectStatement.setInt      (++index2, country);
        createSubjectStatement.setString   (++index2, primaryContactNumber);
        createSubjectStatement.setString   (++index2, secondaryContactNumber);
        createSubjectStatement.setTimestamp(++index2, createStamp);
        createSubjectStatement.setBoolean  (++index2, true);
        createSubjectStatement.setBoolean  (++index2, true);

        createSubjectStatement.executeUpdate();

    }
}
