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

package edu.harvard.catalyst.scheduler.subjectDataCleaner;

import com.opencsv.CSVWriter;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import static org.kohsuke.args4j.ExampleMode.ALL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.Key;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import org.apache.log4j.Logger;

import javax.sql.DataSource;

/**
 * This is a standalone command line application.
 *
 * It traverses the entire set of Subject records in the database, looking for matches. A match means that
 * at least some of the identifying information is the same in two records. The identifying information
 * consists of the MRN, the last name, and the date-of-birth.
 *
 * In addition, all subject records are also analyzed for 3 different types of 'staleness'. Staleness can
 * consist of one of the following three criteria being met:
 *      1. The subject was created more than X months ago, and was never assigned to a study
 *      2. The subject was never scheduled on any study, and was not added to a new recently in the last X months
 *      3. A study to which a subject is associated more than X months ago never had the subject scheduled
 *
 * In addition, subjects are flagged if their MRN needs to be normalized (if it contains blank space and or dashes)
 *
 * This application does not use hibernate, and loads and manipulates all the data in memory. Given the relatively
 * small data sets which will need to be processed (~10,000 subjects), this strategy is the simplest to implement and
 * probably one of the most performant.
 *
 * Subject and study data is queried from the database using straight SQL queries over a jdbc connection.
 *
 * The application is managed by Spring for all its context management goodness.
 *
 * MAJOR ASSUMPTIONS:
 *   there is one and only one MRN for each subject
 *   the column subject.archival_status already exists
 *
 */

@Component
class SubjectDataCleaner {


    /**
     * This inner class represents a subject. It stores only the subject information that will be
     * needed by the algorithm and by the CSV generation method.
     *
     * It also has three flags which are used to indicate the need for curation:
     *      1. mrnNormalizationNeeded: subject's MRN contains spaces and/or dashes
     *      2. oldCreationDateAndNotOnAnyStudy: The subject was created more than X months ago, and was never assigned to a study
     *      3. notAddedToAnyStudyRecentlyAndNeverScheduled: The subject was never scheduled on any study, and was not added to a new recently in the last X months
     *
     * Note: we could have called this class "Subject" but SubjectSummary avoids confusion with the Subject entity
     */
    class SubjectSummary {

        int subjectId;
        String mrn;
        String normalizedMrn;
        boolean mrnIsInvalid;
        String lastName;
        String firstName;
        String gender;
        String streetAddress;
        String city;
        String state;
        String zipcode;
        String country;
        String phone;
        Date birthdate;

        Date dateCreated;
        boolean mrnNormalizationNeeded;
        Match match;
        int monthsSinceCreation;
        boolean oldCreationDateAndNotOnAnyStudy;
        boolean notAddedToAnyStudyRecentlyAndNeverScheduled;
        boolean hasStaleStudies;

        List<StudyAssignment> studies;

        SubjectSummary() {

            studies = new ArrayList<>(20);

        }

    }

    /**
     * This inner enum represents a type of match between two subjects.
     */
    enum MatchConfidence {
        High,
        PartialHigh,
        PartialLow,
        NoMatch
    }

    /**
     * This inner class represents a match between two subjects. It is owned by
     * one of the subjects in the relationship, and points to the other subject,
     * with the match type expressing how the former matches the latter.
     */
    class Match {
        SubjectSummary matchedSubject;
        MatchConfidence matchConfidence;

        Match(SubjectSummary matchedSubject, MatchConfidence matchConfidence) {
            this.matchedSubject = matchedSubject;
            this.matchConfidence = matchConfidence;
        }

    }

    /**
     * This inner class represents the fact that a subject is assigned to a study.
     * It also contains
     *      * the date when the assignment was created (which in the database
     *      exists as a value in the activity_log table), and also the assignedToSubjectLongAgoAndNeverScheduled
     *      * a flag which indicates that this subject probably will never be scheduled to this study.
     */
    class StudyAssignment {

        int studySubjectId;
        String localId;
        String studyName;
        Date dateAddedToStudy;
        int monthsSinceAddedToStudy = 9999; // could be null if some study assignments don't have a matching activity_log entry. but that might only happen with bad test data?
        int numberOfBookedVisists;
        // the following field made sense at some point when we were considering automatically deciding to keep subjects which had
        // been associated with a study less than X months ago, where X was greater than 0.
        // currently the tool is used in default mode where X is 0, so this field does not represent anything useful
//        boolean assignedToSubjectLongAgoAndNeverScheduled;

    }

    /**
     * This inner class represents a group of subjects which match each other. i.e. each subject in the
     * cluster has a match object which points to another member of the cluster. By traversing the match
     * relationships, all members of the cluster can be accessed by any member. In addition there
     * does not exists any other subjec from the database which could be added to the cluster. They're all there.
     *
     * Clusters are given an id, which has no relation to any database id. The is used for grouping subjects by cluster
     * in the output CSV, in case the user changes the order of the rows.
     */
    class Cluster {

        int id;
        List<SubjectSummary> subjects;

        Cluster(int id) {
            this.id = id;
        }
    }


    /**
     * The directory where the output csv will be placed
     */
    static final String DEFAULT_OUTPUT_DIRECTORY = "data-cleaner-output";

    /**
     * The location and name of the output CSV file
     */
    static final String DEFAULT_DUPLICATE_SUBJECTS_TO_CURATE_FILENAME = "duplicateSubjectsToCurate.csv";
    static final String DEFAULT_NON_DUPLICATE_SUBJECTS_TO_CURATE_FILENAME = "nonDuplicateSubjectsToCurate.csv";

    /**
     * utility constant for calculating months from timespans
     */
    static double MILLISECONDS_PER_MONTH = 1000.0 * 60.0 * 60.0 * 24.0 * 30.0;

    /**
     * The SQL query for retrieving ALL subjects from the database, with only the fields that are needed
     * by this application.
     *
     * It is expected that each subject has exaclty one MRN. Subject with more than one MRN will trigger an execption.
     * Subjects with no MRN will be ignored.
     */
    static final String QUERY_ALL_SUBJECTS =
    "SELECT " +
            "su.id, " +
            "sm.mrn, " +
            "su.created_date, " +
            "su.first_name, " +
            "su.last_name, " +
            "su.birthdate, " +
            "su.created_date, " +
            "su.street_address1, " +
            "su.city, " +
            "sta.name as state, " +
            "su.zip, " +
            "c.name as country, " +
            "su.primary_contact_number, " +
            "ge.code as gender " +
            "FROM subject su " +
            "  JOIN country c ON c.id = su.country " +
            "  JOIN state as sta ON sta.id = su.state " +
            "  JOIN subject_mrn as sm ON sm.subject = su.id " +
            "  JOIN gender ge ON ge.id = su.gender " +
            "WHERE " +
            "  su.archival_status IS NULL;";

    /**
     * The SQL query for retrieving subjects created more than X months ago, and which were never assigned to a study
     */
    static final String QUERY_SUBJECTS_WITH_OLD_CREATION_DATE_AND_NOT_ON_ANY_STUDY =
            "SELECT " +
                    "su.id, " +
                    "sm.mrn ," +
                    "su.created_date, " +
                    "su.first_name, " +
                    "su.last_name, " +
                    "su.birthdate, " +
                    "su.created_date, " +
                    "su.street_address1, " +
                    "su.city, " +
                    "sta.name as state, " +
                    "su.zip, " +
                    "c.name as country, " +
                    "su.primary_contact_number, " +
                    "ge.code as gender " +
                    "FROM subject su " +
                    "  JOIN subject_mrn as sm ON sm.subject = su.id " +
                    "  JOIN gender ge ON ge.id = su.gender " +
                    "  JOIN country c ON c.id = su.country " +
                    "  JOIN state as sta ON sta.id = su.state " +
                    "  LEFT JOIN study_subject ss ON ss.subject_mrn = sm.mrn " +
                    "WHERE su.created_date < DATE_SUB(?, INTERVAL ? MONTH) " +
                    "  AND ss.id IS NULL " +
                    "  AND su.archival_status IS NULL";

    /**
     *  The SQL query for retrieving subjects never scheduled on any study,
     *  and which were not added to a study in the last X months
     */
    static final String QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED =
            "SELECT " +
                    "su.id, " +
                    "sm.mrn ," +
                    "su.created_date, " +
                    "su.first_name, " +
                    "su.last_name, " +
                    "su.birthdate, " +
                    "su.created_date, " +
                    "su.street_address1, " +
                    "su.city, " +
                    "sta.name as state, " +
                    "su.zip, " +
                    "c.name as country, " +
                    "su.primary_contact_number, " +
                    "ge.code as gender, " +
                    "  (SELECT MAX(date) FROM activity_log " +
                    "      WHERE affected_subject = su.id " +
                    "      AND action_performed = 'ADD SUBJECT TO STUDY') AS last_assigned_to_a_study " +
                    "FROM subject su " +
                    "  JOIN subject_mrn as sm ON sm.subject = su.id " +
                    "  JOIN gender ge ON ge.id = su.gender " +
                    "  JOIN country c ON c.id = su.country " +
                    "  JOIN state as sta ON sta.id = su.state " +
                    "    WHERE " +
                    "  (SELECT COUNT(*) FROM booked_visit bv " +
                    "   JOIN subject_mrn sm2 ON bv.subject_mrn = sm2.id" +
                    "    WHERE sm2.subject = su.id) = 0" +
                    "   AND " +
                    "  (SELECT COUNT(*) FROM activity_log " +
                    "  WHERE date > DATE_SUB(?, INTERVAL ? MONTH) " +
                    "    AND affected_subject = su.id " +
                    "    AND action_performed = 'ADD SUBJECT TO STUDY') = 0 " +
                    "  AND su.archival_status IS NULL;";

    /**
     * The SQL query for retrieving subjects accompanied by all the studies to which
     * they were assigned long ago and never scheduled. This is distinct from the
     * previous query (QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED)
     * it is used to list all the stale studies for a given subject, but not to detect
     * whether a subject has absolutely no recent study assignments or booked visits
     */
    static final String QUERY_STUDIES_ASSIGNED_TO_SUBJECT_LONG_AGO_AND_NEVER_SCHEDULED =
            "SELECT " +
                    "su.id as subject_id, " +
                    "sm.mrn ," +
                    "su.created_date, " +
                    "su.first_name, " +
                    "su.last_name, " +
                    "su.birthdate, " +
                    "su.created_date, " +
                    "su.street_address1, " +
                    "su.city, " +
                    "sta.name as state, " +
                    "su.zip, " +
                    "c.name as country, " +
                    "su.primary_contact_number ," +
                    "ge.code as gender, " +
                    "ss.id AS study_subject_id, " +
                    "ss.study AS study_id, " +
                    "st.name AS study_name, " +
                    "al.date AS date_added_to_study " +
                    "  FROM subject su " +
                    "    JOIN subject_mrn as sm ON sm.subject = su.id " +
                    "    JOIN gender ge ON ge.id = su.gender " +
                    "    JOIN country c ON c.id = su.country " +
                    "    JOIN state as sta ON sta.id = su.state " +
                    "    LEFT JOIN study_subject ss ON ss.subject_mrn = sm.id " +
                    "    LEFT JOIN study st ON ss.study = st.id " +
                    "    LEFT JOIN activity_log al ON (al.affected_subject = su.id AND al.affected_study = ss.study) " +
                    "  WHERE al.action_performed = 'ADD SUBJECT TO STUDY' " +
                    "  AND al.date < DATE_SUB(?, INTERVAL ? MONTH) " +
                    "  AND (SELECT COUNT(*) FROM booked_visit bv " +
                    "    WHERE bv.subject_mrn = sm.id and bv.study = st.id) = 0 " +
                    "  AND su.archival_status IS NULL;";


    /**
     * SQL query for getting all the studies for a given subject
     *
     * NOTE: order of INNER and LEFT joins matter. see http://stackoverflow.com/questions/9685289/using-left-join-and-inner-join-in-the-same-query
     * and other posts for explanations
     */
    static final String QUERY_ALL_STUDIES_FOR_A_SUBJECT =
            "SELECT " +
                    "su.id as subject_id, " +
                    "ss.id AS study_subject_id, " +
                    "ss.study AS study_id, " +
                    "st.name AS study_name, " +
                    "st.local_id as local_id, " +
                    "al.date AS date_added_to_study, " +
                    "(SELECT COUNT(*) FROM booked_visit bv " +
                    "    JOIN subject_mrn sm ON bv.subject_mrn = sm.id" +
                    "    WHERE sm.subject = su.id) as number_of_booked_visits" +
                    "  FROM subject su " +
                    "    JOIN subject_mrn as sm ON sm.subject = su.id " +
                    "    JOIN gender ge ON ge.id = su.gender " +
                    "    LEFT JOIN study_subject ss ON ss.subject_mrn = sm.id " +
                    "    LEFT JOIN study st ON ss.study = st.id " +
                    "    LEFT JOIN activity_log al ON (al.affected_subject = su.id AND al.affected_study = ss.study) " +
                    "  WHERE su.id = ? " +
                    "    AND al.action_performed = 'ADD SUBJECT TO STUDY' " +
                    "    AND su.archival_status IS NULL;";


    /**
     * The jdbc connection
     */
    DataSource dataSource;

    /*
     * The following few variables are command line options, as per the @Option annotation (args4j)
     */

    /**
     * A command-line option indicating whether or not to decrypt the subject data retrieved from the database.
     * It is a command-line option
     */
    @Option(name = "-help", required = false, usage = "outputs usage info")
    boolean help = false;

    /**
     * A command-line option indicating whether or not to decrypt the subject data retrieved from the database.
     * It is a command-line option
     */
    @Option(name = "-noDecrypt", required = false, usage = "do not decrypt subjects")
    boolean noDecrypt = false;

    @Option(name = "-outputDir", required = false, usage = "the working directory; it will be cleared by the program, and then the output CSV file paths will be relative to it")
    String outputDirectory = DEFAULT_OUTPUT_DIRECTORY;

    // FIXME-XH : the usage information for this option will say that the default value is whatever value was passed on the command line
    // I believe this is an artifact of noDupesFilename being set as a function of the specified output format?
    // To be investigated
    @Option(name = "-dupesFileName", required = false, metaVar = "[filename]", usage = "output file path for duplicate subjects")
    String dupesFilename = DEFAULT_DUPLICATE_SUBJECTS_TO_CURATE_FILENAME;

    // FIXME-XH : the usage information for this option will say that the default value is whatever value was passed on the command line
    // I believe this is an artifact of noDupesFilename being set as a function of the specified output format?
    // To be investigated
    @Option(name = "-noDupesFileName", required = false, metaVar = "[filename]", usage = "output file path for non-duplicate subjects")
    String noDupesFilename = DEFAULT_NON_DUPLICATE_SUBJECTS_TO_CURATE_FILENAME;

    /**
     * A command-line option indicating whether or not to skip subject matches based on first and last name only.
     * This is a utility which helps prevent testing from slowing down due to a restricted first-name / last-name test data space
     */
    @Option(name = "-noMatchOnFullNamesOnly", required = false, usage = "skip subject matches based on first and last name only")
    boolean skipFullnameMatches = false;

    /**
     * A command-line option indicating whether or not to allow subject matches based on last name and birthdate only.
     * This is a utility which helps generate more matches when testing.
     */
    @Option(name = "-matchOnLastnameAndBirthdate", required = false, usage = "match subjects with the same last name and birhtday.")
    boolean matchOnLastnameAndBirthdate = false;

    /**
     * The threshold for the criteria of "old-ness" (or stale-ness) in number of months. Applies to 3 different
     * queries. If each of these queries need a different value for the threshold, the code needs to be changed.
     * It is a command-line option
     */
    @Option(name = "-msu", required = false, metaVar = "[number of months]", usage = "number of months before a subject is considered stale")
    int numberOfMonthsForASubjectToBeStale = 0;

    @Option(name = "-mst", required = false, metaVar = "[number of months]", usage = "number of months before a study assignment is considered stale")
    int numberOfMonthsForAStudySubjectAssignmentToBeStale = 0;

    /**
     * Today's date. It will be set only once when the main class is instantiated. All queries and other comparisons
     * which rely on today's date will use this date, to avoid aberrations
     */
    final Date today;
    final String todayString;
    final long todayMilliseconds;

    boolean decrypt = true;

    int numberOfSubjectsWithOldCreationDateAndNotOnAnyStudy;
    int numberOfSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled;
    int numberOfSubjectsStudiesAddedToStudyLongAgoAndNeverScheduled;

    /**
     * The following are the full output file paths, composed of the outpuDirectory (either defaul or set by the user)
     * and of the CSV file names (note that the CSV file names can be paths, i.e. contain directories; these
     * directories will be created under the outputDirectory
     */
    String dupesFilePath;
    String noDupesFilePath;

    // comparator for ordering subjects in the output files
    final Comparator<SubjectSummary> subjectSummaryComparator = new Comparator<SubjectSummary>() {
        @Override
        public int compare(SubjectSummary leftSubject, SubjectSummary rightSubject) {

            String leftMrn = leftSubject.normalizedMrn.toLowerCase();
            String rightMrn = rightSubject.normalizedMrn.toLowerCase();

            // Take care of the null cases first, because otherwise they will break the code that
            // takes care of non-null values
            if (leftMrn == null) {
                if (rightMrn == null) {
                    return compareByLastName(leftSubject, rightSubject);
                } else {
                    // put null MRNs after non-null MRNs
                    return 1;
                }
            } else if (rightMrn == null) {
                // put null MRNs after non-null MRNs
                return -1;
            } else {
                boolean leftMrnIsANumber;
                boolean rightMrnIsANumber;
                int numericLeftMrn = 0;
                int numericRightMrn = 0;
                // figure out what's a number and what isn't
                try {
                    numericLeftMrn = Integer.parseInt(leftSubject.normalizedMrn);
                    leftMrnIsANumber = true;
                } catch (NumberFormatException e) {
                    leftMrnIsANumber = false;
                }
                try {
                    numericRightMrn = Integer.parseInt(rightSubject.normalizedMrn);
                    rightMrnIsANumber = true;
                } catch (NumberFormatException e) {
                    rightMrnIsANumber = false;
                }
                // based on findings above, order null, bad, and good numeric MRNs
                if (!leftMrnIsANumber) {
                    if (!rightMrnIsANumber) {
                        return compareByLastName(leftSubject, rightSubject);
                    } else {
                        return 1;
                    }
                }
                if (!rightMrnIsANumber) {
                    return -1;
                }
                // lower integers come before
                return numericLeftMrn == numericRightMrn ? compareByLastName(leftSubject, rightSubject) :
                        (numericLeftMrn < numericRightMrn ? -1 : 1);
            }
        }

        private int compareByLastName(SubjectSummary leftSubject, SubjectSummary rightSubject) {

            int comparison = leftSubject.lastName.toLowerCase().compareTo(rightSubject.lastName.toLowerCase());
            if (comparison == 0) return compareByFirstName(leftSubject, rightSubject);
            else return comparison;

        }

        private int compareByFirstName(SubjectSummary leftSubject, SubjectSummary rightSubject) {

            return leftSubject.firstName.toLowerCase().compareTo(rightSubject.firstName.toLowerCase());

        }

    };


    /**
     * Most likely a log4j logger (depending on the configuration). Very useful in concatenation mode, so
     * that we can see the history of what the user has done with the program.
     */
    static final Logger log = Logger.getLogger(SubjectDataCleaner.class);

    /**
     * The entry point!!
     *
     * @param args
     * @throws SQLException
     * @throws IOException
     */
    public static void main(String[] args) {

        // suppress most logging except for this porgram
        LogManager.getRootLogger().setLevel(Level.ERROR);
        LogManager.getLogger(SubjectDataCleaner.class).setLevel(Level.INFO);

        try {
            Date startDate = new Date();

            // Create bean
            ApplicationContext context = new ClassPathXmlApplicationContext("spring-subject-data-cleaner.xml");
            SubjectDataCleaner subjectDataCleaner = (SubjectDataCleaner) context.getBean("subjectDataCleaner");

            // Note, this could be done in the SubjectDataCleaner constructor if we can get the bean factory to
            // invoke the SubjectDataCleaner constructor with the args passed to it. I tried a version
            // of getBean() which is able to pass arguments that way, but could not get it to work.
            subjectDataCleaner.setUp(args);
            subjectDataCleaner.createSubjectCurationLists();

            Date endDate = new Date();
            long elapsed = endDate.getTime() - startDate.getTime();
            subjectDataCleaner.outputToLogAndStdout("Executed in " + elapsed / 1000 + " seconds");

        }
        catch (Exception e) {
            outputToLogAndError("The following exception/error was thrown\n", e);
        }

    }


    /**
     * The main class constructor!
     *
     * Spring injects the datasource (a jdbc connection) and the encryption key.
     * The constructor then makes a record of today's date (which is final, so
     * must be done in the constructor) and the encryption key,
     * for later use.
     *
     * @param dataSource
     * @param key
     */
    @Autowired
    SubjectDataCleaner(DataSource dataSource,
                       @Qualifier(value = "encryptionKeyForSubjectDataCleanUp") final Key key) {

        // save the data source when the bean is created
        this.dataSource = dataSource;

        // set up the encrypter with the required key
        SubjectDataEncryptor.setEncryptionKey(key);

        // set up today's date (so we use the exact same date in all SQL queries)
        // these are final variables so need to be set in the constructor
        this.today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        this.todayString = dateFormat.format(this.today);
        this.todayMilliseconds = this.today.getTime();

    }

    private void setUp(String[] args) throws IOException {

        // parse the command line and exit on parsing error or request for help. Note, this
        // could be done in the SubjectDataCleaner constructor if we can get the bean factory to
        // invoke the SubjectDataCleaner constructor with the args passed to it. I tried a version
        // of getBean() which is able to pass arguments that way, but could not get it to work.
        this.parseCommandLineAndExitIfNeeded(args);

        // a little bit of feedback on how the parsed arguments affected the object's variables:
        this.outputToLogAndStdout("Runnind Subject Data Cleaner on " + this.todayString + " with the following input parameters:");
        this.outputToLogAndStdout("decrypt: " + this.decrypt);
        this.outputToLogAndStdout("numberOfMonthsForASubjectToBeStale: " + this.numberOfMonthsForASubjectToBeStale);
        this.outputToLogAndStdout("numberOfMonthsForAStudySubjectAssignmentToBeStale: " + this.numberOfMonthsForAStudySubjectAssignmentToBeStale);
        this.outputToLogAndStdout("base output directory (will be cleared): " + this.outputDirectory);
        this.outputToLogAndStdout("output file for duplicates: " + this.dupesFilePath);
        this.outputToLogAndStdout("output file for non-dupicates: " + this.noDupesFilePath);

        // clean-up output directory
        // TODO-XH / FIXME : clear the directory specified by the user, not the default one
        File outputDir = new File(this.outputDirectory);
        outputDir.mkdirs();
        for (File file : outputDir.listFiles()) {
            FileDeleteStrategy.FORCE.delete(file);
        }

    }


    /**
     * A crude way to duplicate logging to stdout. log4j offers a mechanism for
     * creating custom logging levels, but it was much simpler to do the following hack.
     * It's good enough for now.
     * @param message
     */
    static void outputToLogAndStdout(String message) {

        log.info(message);
        System.out.println(message);

    }

    /**
     * A crude way to duplicate logging to stdout. log4j offers a mechanism for
     * creating custom logging levels, but it was much simpler to do the following hack.
     * It's good enough for now.
     * @param message
     */
    static void outputToLogAndError(String message) {

        log.error(message);
        System.err.println(message);

    }

    static void outputToLogAndError(String message, Throwable t) {

        log.error(message, t);
        System.err.println(message);
        t.printStackTrace(System.err);

    }

    /**
     * Parse command line arguments and options uing args4j
     * See https://github.com/kohsuke/args4j/blob/master/args4j/examples/SampleMain.java
     * for sample code
     * @param args the same String[] array that is passed to the main static method
     */
    void parseCommandLineAndExitIfNeeded(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);
            // show help screen?
            if (this.help) {
                printUsageAndExampleCommandLine(parser);
                System.exit(0);
            }
        } catch (CmdLineException e) {
            outputToLogAndError(e.getMessage());
            printUsageAndExampleCommandLine(parser);
            System.exit(-1);
        }

        // process the command line options which have an impact on the subjectDataCleaner object
        this.decrypt = !this.noDecrypt;
        this.dupesFilePath = this.outputDirectory + "/" + this.dupesFilename;
        this.noDupesFilePath = this.outputDirectory + "/" + this.noDupesFilename;

    }

    void printUsageAndExampleCommandLine(CmdLineParser parser) {
        System.err.println("Usage:");
        System.err.println("  java " + this.getClass().getName() + " [options...]");
        System.err.println("where the options are:");
        // print the list of available options
        // FIXME : usage displays filename passed as argument as default!
        parser.printUsage(System.err);
        // print example command-line
        System.err.println("  Example: java " + this.getClass().getName() + " " + parser.printExample(ALL));
    }


    /**
     * The main loop!! Once all the set-up leg-work has been taken care of, this is where things happen
     *
     * @throws SQLException
     * @throws IOException
     */
    void createSubjectCurationLists() throws SQLException, IOException {

        // poor-man's profiling
        Date startDate = new Date();

        // Create prepared statements.
        // Note: all the queries need to happen within the same try block. Otherwise
        // prepared statements get closed.
        try (Connection dbConnection = this.dataSource.getConnection()) {
            PreparedStatement queryAllSubjectsPreparedStatement =
                    dbConnection.prepareStatement(QUERY_ALL_SUBJECTS);

            PreparedStatement querySubjectsWithOldCreationDateAndNotOnAnyStudyPreparedStatement =
                    dbConnection.prepareStatement(QUERY_SUBJECTS_WITH_OLD_CREATION_DATE_AND_NOT_ON_ANY_STUDY);

            PreparedStatement querySubjectsNotAddedToAnyStudyRecentlyAndNeverScheduledPreparedStatement =
                    dbConnection.prepareStatement(QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED);

            PreparedStatement queryStudiesAssignedToSubjectLongAgoAndNeverScheduledPreparedStatement =
                    dbConnection.prepareStatement(QUERY_STUDIES_ASSIGNED_TO_SUBJECT_LONG_AGO_AND_NEVER_SCHEDULED);

            PreparedStatement queryAllStudiesForASubjectPreparedStatement =
                    dbConnection.prepareStatement(QUERY_ALL_STUDIES_FOR_A_SUBJECT);


            // This is where the list of 'clusters' of matching subjects will be accumulated as the data set is traversed
            List<Cluster> matchClusters = new ArrayList();
            // Initially, we get the whole set of subjects as a map of id to SubjectSummary object of the same id so that
            // later we can find subjects by id
            Map<Integer, SubjectSummary> allSubjectsMap = getAllSubjects(queryAllSubjectsPreparedStatement);
            // This is also the exhaustive list of subjects, but stored as a list, not a map. The reason is that as we traverse
            // this list we will remove items from it after they've been matched. Meanwhile, we preserve the map intact.
            // TODO-XH : is this really needed? can we do with only the map?
            List<SubjectSummary> subjectsList = new ArrayList<>(allSubjectsMap.values());
            // Maps of subjects who meet one of the criteria for needing curation. These are maps instead of lists, so that
            // it's easy to find out, given a Subject id, if it qualifies for any of the curation criteria
            Map<Integer, SubjectSummary> subjectsWithOldCreationDateAndNotOnAnyStudy =
                    getSubjectsWithOldCreationDateAndNotOnAnyStudy(
                            querySubjectsWithOldCreationDateAndNotOnAnyStudyPreparedStatement,
                            allSubjectsMap);
            Map<Integer, SubjectSummary> subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled =
                    getSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled(
                            querySubjectsNotAddedToAnyStudyRecentlyAndNeverScheduledPreparedStatement,
                            allSubjectsMap);
            Map<Integer, SubjectSummary> subjectsStudiesAddedToStudyLongAgoAndNeverScheduled =
                    getStudiesAssignedToSubjectLongAgoAndNeverScheduled(
                            queryStudiesAssignedToSubjectLongAgoAndNeverScheduledPreparedStatement,
                            allSubjectsMap);


            this.numberOfSubjectsWithOldCreationDateAndNotOnAnyStudy = subjectsWithOldCreationDateAndNotOnAnyStudy.size();
            this.numberOfSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled = subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled.size();
            this.numberOfSubjectsStudiesAddedToStudyLongAgoAndNeverScheduled = subjectsStudiesAddedToStudyLongAgoAndNeverScheduled.size();

            int clusterIndex = 0;

            while (!subjectsList.isEmpty()) {
                // Look for a new cluster based on the head subject
                // and remove the subject from the list, because it will have been processed in this iteration of the loop
                SubjectSummary subject = subjectsList.remove(0);
                // findMatches() will return a list of matching subjects, after removing these subjects from the master list
                List<SubjectSummary> matchingSubjects = findMatches(subjectsList, subject);
                // put matching subjects in a cluster
                if (matchingSubjects.size() > 0) {
                    // create a cluster
                    Cluster cluster = new Cluster(clusterIndex++);
                    cluster.subjects = matchingSubjects;
                    // don't forget to add the subject that has all these matches, at the head of the list
                    cluster.subjects.add(0, subject);
                    matchClusters.add(cluster);

                    // now see if the cluster contains any stale subjects
                    cluster.subjects.stream().forEach(matchedSubject -> {
                        mergeStaleSubjects(matchedSubject, subjectsWithOldCreationDateAndNotOnAnyStudy, subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled, subjectsStudiesAddedToStudyLongAgoAndNeverScheduled);
                    });

                }

                this.outputToLogAndStdout("Processed subject id " + subject.subjectId);
            }


            // Now process the 'left-over' subjects who don't belong to any cluster
            // Get them from the remaining contents of the maps of 'stale' objects
            // but merging the information from all 3 maps into a single subject object
            // to avoid listing them twice.
            // Remember that the subjects in the first two set of stale subjects are identical
            // to the subjects in the allSubjectsMap; they are not copies.

            SortedSet<SubjectSummary> nonClusteredSubjectsNeedingCuration = new TreeSet<>(this.subjectSummaryComparator);

            subjectsWithOldCreationDateAndNotOnAnyStudy.entrySet().stream().forEach(entry -> {
                SubjectSummary subject = entry.getValue();
                mergeStaleSubjects(subject, null, subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled, subjectsStudiesAddedToStudyLongAgoAndNeverScheduled);
                nonClusteredSubjectsNeedingCuration.add(subject);
            });

            subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled.entrySet().stream().forEach(entry -> {
                SubjectSummary subject = entry.getValue();
                mergeStaleSubjects(subject, null, null, subjectsStudiesAddedToStudyLongAgoAndNeverScheduled);
                nonClusteredSubjectsNeedingCuration.add(subject);
            });

            // remaining stale study assignments, where the subject was not in a cluster, and not in either of
            // the other sets of stale subject
            subjectsStudiesAddedToStudyLongAgoAndNeverScheduled.entrySet().stream().forEach(entry -> {
                SubjectSummary subject = entry.getValue();
                nonClusteredSubjectsNeedingCuration.add(subject);
            });

            // poor-man's profiling
            Date endDate = new Date();
            long elapsed = endDate.getTime() - startDate.getTime();
            this.outputToLogAndStdout("Processed all subjects in " + elapsed / 1000 + " seconds");

            // Add studies to the first curation list
            Iterator<Cluster> clusterIterator = matchClusters.iterator();
            while (clusterIterator.hasNext()) {
                Cluster cluster = clusterIterator.next();
                Iterator<SubjectSummary> subjectIterator = cluster.subjects.iterator();
                while (subjectIterator.hasNext()) {
                    SubjectSummary subject = subjectIterator.next();
                    subject.studies = getStudiesForSubject(queryAllStudiesForASubjectPreparedStatement, subject.subjectId);
                }
            }

            // Add the studies to the subjects of the 2nd curation list
            Iterator<SubjectSummary> subjectIterator = nonClusteredSubjectsNeedingCuration.iterator();
            while (subjectIterator.hasNext()) {
                SubjectSummary subject = subjectIterator.next();
                subject.studies = getStudiesForSubject(queryAllStudiesForASubjectPreparedStatement, subject.subjectId);
            }

            // generate the first curation list (clusters)
            generateCsvForDupes(matchClusters);

            // generate the second curation list
            generateCsvForNonDupes(nonClusteredSubjectsNeedingCuration);

        }

    }

    /**
     * This method modifies the subject object passed to it. It may set the 'staleness' flags of the subject
     * and/or add to its list of stale study assignments.
     *
     * Any of the arguments execept subject can be set to null, with the effect of skipping looking for the subject in that type of staleness.
     * This is useful when trying to roll-up all the staleness flags to single subjects.
     * // TODO-XH: I think this is redundant if we are removing the subjects from the 'stale subject' lists as we go
     *
     * @param subject
     * @param subjectsWithOldCreationDateAndNotOnAnyStudy
     * @param subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled
     * @param subjectsStudiesAddedToStudyLongAgoAndNeverScheduled
     * @return
     */
    boolean mergeStaleSubjects(SubjectSummary subject,
                               Map<Integer, SubjectSummary> subjectsWithOldCreationDateAndNotOnAnyStudy,
                               Map<Integer, SubjectSummary> subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled,
                               Map<Integer, SubjectSummary> subjectsStudiesAddedToStudyLongAgoAndNeverScheduled) {

        boolean mergeOccurred = false;

        if (subjectsWithOldCreationDateAndNotOnAnyStudy != null) {
            SubjectSummary staleSubject = subjectsWithOldCreationDateAndNotOnAnyStudy.get(subject.subjectId);
            if (staleSubject != null) {
                // the subject was found in the map of subjects matching the criteria for needing curation.
                // So let's just update some the staleness flags on the subject
                // and remove it from the 'stale subjects' map (here, subjectsWithOldCreationDateAndNotOnAnyStudy).
                subjectsWithOldCreationDateAndNotOnAnyStudy.remove(staleSubject.subjectId);
                subject.oldCreationDateAndNotOnAnyStudy = true;
                mergeOccurred = true;
            }
        }
        if (subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled != null) {
            SubjectSummary staleSubject = subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled.get(subject.subjectId);
            if (staleSubject != null) {
                // the subject was found in the map of subjects matching the criteria for needing curation.
                // So let's just update some the staleness flags on the subject
                // and remove it from the 'stale subjects' map (here, subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled).
                subjectsNotAddedToAnyStudyRecentlyAndNeverScheduled.remove(staleSubject.subjectId);
                subject.notAddedToAnyStudyRecentlyAndNeverScheduled = true;
                mergeOccurred = true;
            }
        }
        // Stale studies were found for this subject. Add them to the subject, and remove them
        // from the 'stale studies' map (here: subjectsStudiesAddedToStudyLongAgoAndNeverScheduled)
        if (subjectsStudiesAddedToStudyLongAgoAndNeverScheduled != null) {
            SubjectSummary staleSubject = subjectsStudiesAddedToStudyLongAgoAndNeverScheduled.get(subject.subjectId);
            if (staleSubject != null) {
                // the subject was found in the map of subjects matching the criteria for needing curation.
                // So let's just update some the staleness flags on the subject
                // and remove it from the 'stale subjects' map (here, subjectsStudiesAddedToStudyLongAgoAndNeverScheduled).
                subjectsStudiesAddedToStudyLongAgoAndNeverScheduled.remove(staleSubject.subjectId);
                subject.hasStaleStudies = true;
                mergeOccurred = true;
            }
        }

        return mergeOccurred;

    }


    Map<Integer, SubjectSummary> getAllSubjects(PreparedStatement preparedStatement) throws SQLException {

        log.info("Running the following query for getAllSubjects() :\n" + QUERY_ALL_SUBJECTS);

        Date startDate = new Date();

        // we know the list will contain around 15,000 subjects in the
        // worst case, at the time of this writing. So allocate that
        // number plus some buffer for if the number of subjects
        // grows between now and the time that this program is run
        Map<Integer, SubjectSummary> subjects = new HashMap<>(20000);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            SubjectSummary subject = makeSubjectSummaryFromResultSet(resultSet);
            // even though the subject_mrn and subject tables are N-to-1, in standalone mode (which is the mode that the
            // the application must necessarily be when this program is run) we expect there to be exactly one MRN per subject)
            if (subjects.containsKey(subject.subjectId)) {
                throw new RuntimeException("Subject ID " + subject.subjectId + " found multiple times (probably with multiple MRNs in database," +
                                           " including the following MRN values: " + subject.mrn + " and " + subjects.get(subject.subjectId).mrn + ")");
            }
            subjects.put(subject.subjectId, subject);

        }

        this.outputToLogAndStdout("Found " + subjects.size() + " subjects in the database");

        Date endDate = new Date();
        long elapsed = endDate.getTime() - startDate.getTime();
        this.outputToLogAndStdout("Got all subjects from database in " + elapsed / 1000 + " seconds");

        return subjects;

    }

    Map<Integer, SubjectSummary> getSubjectsWithOldCreationDateAndNotOnAnyStudy(PreparedStatement preparedStatement, Map<Integer, SubjectSummary> allSubjectsMap) throws SQLException {

        log.info("Running the following query for getSubjectsWithOldCreationDateAndNotOnAnyStudy() :\n" + QUERY_SUBJECTS_WITH_OLD_CREATION_DATE_AND_NOT_ON_ANY_STUDY);

        // This method will insert existing subjects into a new Map for those who meet the
        // criteria for curation. The subjes objects are NOT duplicated.

        Map<Integer, SubjectSummary> subjectsMap = new HashMap<>(20000);

        preparedStatement.setDate(1, new java.sql.Date(todayMilliseconds));
        preparedStatement.setInt(2, this.numberOfMonthsForASubjectToBeStale);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            int subjectId = resultSet.getInt("id");
            SubjectSummary subject = allSubjectsMap.get(subjectId);
            subject.oldCreationDateAndNotOnAnyStudy = true;
            subjectsMap.put(subjectId, subject);

        }

        this.outputToLogAndStdout("Found " + subjectsMap.size() + " subjects WithOldCreationDateAndNotOnAnyStudy");

        return subjectsMap;

    }

    Map<Integer, SubjectSummary> getSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled(PreparedStatement preparedStatement, Map<Integer, SubjectSummary> allSubjectsMap) throws SQLException {

        log.info("Running the following query for getSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled() :\n" + QUERY_SUBJECTS_NOT_ADDED_TO_ANY_STUDY_RECENTLY_AND_NEVER_SCHEDULED);

        Map<Integer, SubjectSummary> subjectsMap = new HashMap<>(20000);

        preparedStatement.setDate(1, new java.sql.Date(todayMilliseconds));
        preparedStatement.setInt(2, this.numberOfMonthsForAStudySubjectAssignmentToBeStale);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            int subjectId = resultSet.getInt("id");
            SubjectSummary subject = allSubjectsMap.get(subjectId);
            subject.notAddedToAnyStudyRecentlyAndNeverScheduled = true;
            subjectsMap.put(subjectId, subject);

        }

        this.outputToLogAndStdout("Found " + subjectsMap.size() + " subjects NotAddedToAnyStudyRecentlyAndNeverScheduled");

        return subjectsMap;

    }

    /**
     * This method operates in a slightly non-intuitive way. It performs a query which may return
     * multiple rows for one subject (i.e. multiple studies for a single subject). When that happens
     * the subject will be added to the output map multiple times -- but each new insertion in the map
     * will clober the previous insertion. In the end all the information that will be used to generate
     * the report is the mere fact that the subject has stale studies, which will be shown as a single
     * flag (no dates or other information will be used from this map)
     * @param preparedStatement
     * @param allSubjectsMap
     * @return
     * @throws SQLException
     */
    Map<Integer, SubjectSummary> getStudiesAssignedToSubjectLongAgoAndNeverScheduled(PreparedStatement preparedStatement, Map<Integer, SubjectSummary> allSubjectsMap) throws SQLException {

        log.info("Running the following query for getStudiesAssignedToSubjectLongAgoAndNeverScheduled() :\n" + QUERY_STUDIES_ASSIGNED_TO_SUBJECT_LONG_AGO_AND_NEVER_SCHEDULED);

        Map<Integer, SubjectSummary> subjectsToStudiesMap = new HashMap<>(20000);

        preparedStatement.setDate(1, new java.sql.Date(todayMilliseconds));
        preparedStatement.setInt(2, this.numberOfMonthsForAStudySubjectAssignmentToBeStale);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            int subjectId = resultSet.getInt("subject_id");
            SubjectSummary subject = allSubjectsMap.get(subjectId);
            subjectsToStudiesMap.put(subjectId, subject);

        }

        this.outputToLogAndStdout("Found " + subjectsToStudiesMap.size() + " subjects assigned to studies long ago and never scheduled");

        return subjectsToStudiesMap;

    }


    List<StudyAssignment> getStudiesForSubject(PreparedStatement preparedStatement, int subjectId) throws SQLException {


        log.info("Running the following query for getStudiesForSubject() :\n" + QUERY_ALL_STUDIES_FOR_A_SUBJECT);

        List<StudyAssignment> studies = new ArrayList<>(20);

        preparedStatement.setInt(1, subjectId);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            StudyAssignment study = makeStudyAssignmentFromResultSet(resultSet);
            studies.add(study);

        }

        return studies;
    }


    long monthsSinceDate(Date date) {

        return (long)(Math.floor(this.todayMilliseconds - date.getTime()) / MILLISECONDS_PER_MONTH);

    }


    SubjectSummary makeSubjectSummaryFromResultSet(ResultSet resultSet) throws SQLException {

        SubjectSummary subject = new SubjectSummary();

        // fields which are never encrypted

        subject.subjectId = resultSet.getInt("id");
        subject.gender = resultSet.getString("gender");
        subject.dateCreated = resultSet.getDate("created_date");
        subject.monthsSinceCreation = (int) monthsSinceDate(subject.dateCreated);
        subject.birthdate = resultSet.getDate("birthdate");
        subject.state = resultSet.getString("state");
        subject.gender = resultSet.getString("gender");
        subject.country = resultSet.getString("country");

        // fields which may be encrypted

        if (this.decrypt) {
            subject.mrn = SubjectDataEncryptor.decrypt(resultSet.getString("mrn"));
            subject.firstName = SubjectDataEncryptor.decrypt(resultSet.getString("first_name"));
            subject.lastName = SubjectDataEncryptor.decrypt(resultSet.getString("last_name"));
            subject.streetAddress = SubjectDataEncryptor.decrypt(resultSet.getString("street_address1"));
            subject.city = SubjectDataEncryptor.decrypt(resultSet.getString("city"));
            subject.zipcode = SubjectDataEncryptor.decrypt(resultSet.getString("zip"));
            subject.phone = SubjectDataEncryptor.decrypt(resultSet.getString("primary_contact_number"));
        }
        else {
            subject.mrn = resultSet.getString("mrn");
            subject.firstName = resultSet.getString("first_name");
            subject.lastName = resultSet.getString("last_name");
            subject.streetAddress = resultSet.getString("street_address1");
            subject.city = resultSet.getString("city");
            subject.zipcode = resultSet.getString("zip");
            subject.phone = resultSet.getString("primary_contact_number");
        }

        // deal with MRN normalization
        subject.mrn = subject.mrn == null ? "" : subject.mrn;
        subject.normalizedMrn = subject.mrn.replace("-", "").replace(" ", "");
        // whether the MRN needed to be normalized or not, we're setting the normalizedMrn value
        // this way we can access normalized for any pair of subjects, and see if they match, without
        // branching on whether the MRN needed to be normalized or not
        if (subject.normalizedMrn.equals(subject.mrn)) {
            subject.mrnNormalizationNeeded = false;
        } else {
            subject.mrnNormalizationNeeded = true;
        }
        int numericMrn;
        boolean mrnIsNumeric;
        // we're just checking if the MRN parses, but we don't need to keep the value
        try {
            numericMrn = Integer.parseInt(subject.normalizedMrn);
            mrnIsNumeric = true;
        } catch (NumberFormatException e) {
            mrnIsNumeric = false;
        }
        subject.mrnIsInvalid = !mrnIsNumeric;

        return subject;

    }

    StudyAssignment makeStudyAssignmentFromResultSet(ResultSet resultSet) throws SQLException {

        StudyAssignment study = new StudyAssignment();
        study.studySubjectId = resultSet.getInt("subject_id");
        study.localId = resultSet.getString("local_id");
        study.studyName  = resultSet.getString("study_name");
        study.dateAddedToStudy = resultSet.getDate("date_added_to_study");
        study.monthsSinceAddedToStudy = study.dateAddedToStudy != null ? (int)monthsSinceDate(study.dateAddedToStudy) : -1;
        study.numberOfBookedVisists = resultSet.getInt("number_of_booked_visits");
        // the following only makes sense when the threshold is great than 0. The boolean expression needs to be tweaked
        // so that it is still meaningful when the threshold is 0
        // For now the institutions use a threshold set to 0 so we're not going to fix this now.
//        study.assignedToSubjectLongAgoAndNeverScheduled = (study.monthsSinceAddedToStudy >= this.numberOfMonthsForAStudySubjectAssignmentToBeStale) &&
//                                                          numberOfBookedVisists == 0;

        return study;
    }


    /***
     * This method works as follows:
     * It looks for matches for the SubjectSummary passed to it.
     * (1) It is recursive. It finds a _tree_ of matches
     * (2) it removes from the master list every matchSubject that it finds.
     * @param subjects
     * @param subject
     * @return
     */
    // findMatches() will remove return a cluster's list of subjects, after removing them from the master list
    //
    List<SubjectSummary> findMatches(List<SubjectSummary> subjects, SubjectSummary subject) {

        List<SubjectSummary> matches = new ArrayList<>(100); // the list is EXTREMELY unlikely to need to grow beyond this initial allocation
        // create a fresh iterator to go over the list of all yet unprocessed subjects
        ListIterator<SubjectSummary> subjectsIterator = subjects.listIterator();
        // subjects list gets modified in this loop
        while (subjectsIterator.hasNext()) {
            SubjectSummary matchCandidate = subjectsIterator.next();
            MatchConfidence matchConfidence = match(subject, matchCandidate);
            if (matchConfidence != MatchConfidence.NoMatch) {
                matches.add(matchCandidate);
                // remove the matchCandidate subject from the master list; since it will now be part of a cluster we should not re-visit it
                subjectsIterator.remove();
                // add a Match object to the matchCandidate
                matchCandidate.match = new Match(subject, matchConfidence);
                // RECURSIVE method call. This will try to match the newly matched candidate with subjects from the entire list, from the beginning
                // though not the subjects that have already been matched (since these have been removed from the master list of subjects)
                // The reason we need to go back to the beginning of the list to find new matches is that we need to find subjects who,
                // even though they didn't match any prior subject in the cluster, match the newly found match
                List<SubjectSummary> subMatches = findMatches(subjects, matchCandidate);
                matches.addAll(subMatches);
                // RESET iterator, because list may have changed not through this iterator but through the recursive function call's iterator
                // So the current iterator is no longer valid / predictable
                subjectsIterator = subjects.listIterator();
            }
        }

        return matches;

    }

    MatchConfidence match(SubjectSummary subject, SubjectSummary matchCandidate) {

        if (matchCandidate.subjectId == subject.subjectId) {
            throw new RuntimeException("Came across a matchSubject candidate with the same subjecId as the subject being compared to!");
        }

        boolean mrnsMatch = subject.normalizedMrn.equals(matchCandidate.normalizedMrn);
        boolean firstNamesMatch = subject.firstName.replace(" ", "").equals(matchCandidate.firstName.replace(" ", ""));
        boolean lastNamesMatch = subject.lastName.replace(" ", "").equals(matchCandidate.lastName.replace(" ", ""));
        // if both dates have no hours/minutes/seconds set, the following should work
        boolean birthdatesMatch = subject.birthdate.equals(matchCandidate.birthdate);

        if (mrnsMatch) {
            if (firstNamesMatch && lastNamesMatch && birthdatesMatch) return MatchConfidence.High;
            if (lastNamesMatch && birthdatesMatch) return MatchConfidence.PartialHigh;
            if (firstNamesMatch && birthdatesMatch) return MatchConfidence.PartialHigh;
            if (this.skipFullnameMatches) return MatchConfidence.NoMatch;
            if (firstNamesMatch && lastNamesMatch) return MatchConfidence.PartialHigh;
            return MatchConfidence.PartialLow;
        } else {
            if (this.matchOnLastnameAndBirthdate && lastNamesMatch && birthdatesMatch) return MatchConfidence.PartialLow;
            if (this.skipFullnameMatches) return MatchConfidence.NoMatch;
            if (firstNamesMatch && lastNamesMatch) return MatchConfidence.PartialLow;
            else return MatchConfidence.NoMatch;
        }
    }


    /**
     * generates the CSV output file
     *
     * @param clusters the list of subjects that have at least one match, clustered together with their study assignments
     * @throws IOException
     */
    void generateCsvForDupes(List<Cluster> clusters) throws IOException {

        try (FileWriter csvFileWriter = new FileWriter(this.dupesFilePath);
             CSVWriter csvWriter = new CSVWriter(csvFileWriter, ',');
        ) {
            generateCsvHeader(csvWriter, "SUBJECT DATA CURATION LIST");
            generateCsvBlankLine(csvWriter);
            generateCsvDoublet(csvWriter, "Date:", this.todayString);
            generateCsvHeader(csvWriter, "Program parameters");
            generateCsvDoublet(csvWriter, "decrypt:", String.valueOf(this.decrypt));
            generateCsvDoublet(csvWriter, "output file name:", this.dupesFilePath);
            generateCsvDoublet(csvWriter, "number of months for subjects to be considered stale:", String.valueOf(this.numberOfMonthsForASubjectToBeStale));
            generateCsvDoublet(csvWriter, "number of months for an assignment of a subject to a study to be considered stale:", String.valueOf(this.numberOfMonthsForAStudySubjectAssignmentToBeStale));
            generateCsvBlankLine(csvWriter);
            generateCsvDoublet(csvWriter, "Number Of Subjects With Old Creation Date And Not On Any Study:", String.valueOf(this.numberOfSubjectsWithOldCreationDateAndNotOnAnyStudy));
            generateCsvDoublet(csvWriter, "Number Of Subjects Not Added To Any Study Recently And Never Scheduled:", String.valueOf(this.numberOfSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled));
            generateCsvDoublet(csvWriter, "Number Of Studies Assigned to Subject Long Ago And Never Scheduled:", String.valueOf(this.numberOfSubjectsStudiesAddedToStudyLongAgoAndNeverScheduled));
            generateCsvBlankLine(csvWriter);

            generateCsvHeader(csvWriter, "SUBJECTS WITH MATCHES, NEEDING CURATION");
            generateCsvBlankLine(csvWriter);
            generateCsvBlankLine(csvWriter);
            generateCsvHeadings(csvWriter);
            generateCsvBlankLine(csvWriter);

            clusters.stream().forEach(cluster -> {

                generateCsvBlankLine(csvWriter);

                cluster.subjects.stream().forEach(subject -> {

                    generateSubjectAndItsStudies(csvWriter, subject);

                });
            });

            generateCsvBlankLine(csvWriter);
            generateCsvBlankLine(csvWriter);

        }
    }

    /**
     * generates the CSV output file
     *
     * @param nonClusteredSubjectsNeedingCuration the list of non-clustered (i.e. not matched) subjects which have at
     *                                            least one reason for needing curation (i.e. stale in some respect, or
     *                                            whose MRN needs normalizing)
     * @throws IOException
     */
    void generateCsvForNonDupes(SortedSet<SubjectSummary> nonClusteredSubjectsNeedingCuration) throws IOException {

        try (FileWriter csvFileWriter = new FileWriter(this.noDupesFilePath);
             CSVWriter csvWriter = new CSVWriter(csvFileWriter, ',');
        ) {
            generateCsvHeader(csvWriter, "SUBJECT DATA CURATION LIST");
            generateCsvBlankLine(csvWriter);
            generateCsvDoublet(csvWriter, "Date:", this.todayString);
            generateCsvHeader(csvWriter, "Program parameters");
            generateCsvDoublet(csvWriter, "decrypt:", String.valueOf(this.decrypt));
            generateCsvDoublet(csvWriter, "output file name:", this.dupesFilePath);
            generateCsvDoublet(csvWriter, "number of months for subjects to be considered stale:", String.valueOf(this.numberOfMonthsForASubjectToBeStale));
            generateCsvDoublet(csvWriter, "number of months for an assignment of a subject to a study to be considered stale:", String.valueOf(this.numberOfMonthsForAStudySubjectAssignmentToBeStale));
            generateCsvBlankLine(csvWriter);
            generateCsvDoublet(csvWriter, "Number Of Subjects With Old Creation Date And Not On Any Study:", String.valueOf(this.numberOfSubjectsWithOldCreationDateAndNotOnAnyStudy));
            generateCsvDoublet(csvWriter, "Number Of Subjects Not Added To Any Study Recently And Never Scheduled:", String.valueOf(this.numberOfSubjectsNotAddedToAnyStudyRecentlyAndNeverScheduled));
            generateCsvDoublet(csvWriter, "Number Of Studies Assigned to Subject Long Ago And Never Scheduled:", String.valueOf(this.numberOfSubjectsStudiesAddedToStudyLongAgoAndNeverScheduled));
            generateCsvBlankLine(csvWriter);

            generateCsvHeader(csvWriter, "SUBJECTS WITH NO MATCHES, BUT IN NEED OF CURATION");
            generateCsvBlankLine(csvWriter);
            generateCsvBlankLine(csvWriter);
            generateCsvHeadings(csvWriter);
            generateCsvBlankLine(csvWriter);

            nonClusteredSubjectsNeedingCuration.stream().forEach(subject -> {

                generateSubjectAndItsStudies(csvWriter, subject);

            });

            // TODO-XH: this should not be needed with a try-with-resource if writers are auto-closable.
            // The auto-close behavior will close first allSubjectsCsvWriter and then allSubjectsCsvFileWriter.
            // but here we have to close them in the opposite order for the program to generate the expected output!!!?!??
            // might a bug in CsvWriter
            csvWriter.flush();
            csvFileWriter.flush();
        }

    }

    /**
     * helper for CSV generation.
     * Generates a subject and all its associated studies
     */
    void generateSubjectAndItsStudies(CSVWriter csvWriter, SubjectSummary subject) {

        Object[] studies = subject.studies.toArray();
        StudyAssignment firstStudy = studies.length > 0 ? (StudyAssignment) studies[0] : null;

        generateCsvSubject(csvWriter, subject, true, firstStudy);

        for (int i = 1; i < studies.length; i++) {
            generateCsvSubject(csvWriter, subject, false, (StudyAssignment) studies[i]);
        }

    }


    /**
     * helper for CSV generation
     * @param csvWriter
     */
    void generateCsvBlankLine(CSVWriter csvWriter) {

        String[] blankLine = {""};
        csvWriter.writeNext(blankLine);

    }

    /**
     * helper for CSV generation
     * @param csvWriter
     */
    void generateCsvHeader(CSVWriter csvWriter, String header) {

        String[] entry = new String[1];
        entry[0] = header;
        csvWriter.writeNext(entry);

    }

    /**
     * helper for CSV generation
     * @param csvWriter
     */
    void generateCsvDoublet(CSVWriter csvWriter, String name, String value) {

        String[] entry = new String[2];
        entry[0] = name;
        entry[1] = value;
        csvWriter.writeNext(entry);

    }

    /**
     * helper for CSV generation
     * @param csvWriter
     */
    void generateCsvHeadings(CSVWriter csvWriter) {

        String[] entries = new String[21];

        int index = 0;
        entries[index++] = "Scheduler ID";
        entries[index++] = "MRN";
        entries[index++] = "Suggested MRN";
        entries[index++] = "Time Since Creation";
        entries[index++] = "Study Name";
        entries[index++] = "Local ID";
        entries[index++] = "Time Since Added to Study";
        entries[index++] = "Scheduled? (Y/N)";
        entries[index++] = "Last Name";
        entries[index++] = "First Name";
        entries[index++] = "DOB";
        entries[index++] = "Gender";
        entries[index++] = "Street Addr 1";
        entries[index++] = "City";
        entries[index++] = "State";
        entries[index++] = "Zip Code";
        entries[index++] = "Country";
        entries[index++] = "Primary Phone";
        entries[index++] = "Expected MRN";
        entries[index++] = "Delete Subject?";
        entries[index++] = "Remove Subject from Study?";

        csvWriter.writeNext(entries);

    }

    /**
     * helper for CSV generation
     * @param csvWriter
     */
    void generateCsvSubject(CSVWriter csvWriter, SubjectSummary subject, boolean isFirst, StudyAssignment study) {

        String[] entries = new String[18];

        // The MRN and suggested MRN are outputted in this somewhat surprising format so they will be
        // considered as strings and not numbers by Excel when the CSV data is imported.
        int index = 0;
        entries[index++] = String.valueOf(subject.subjectId);
        entries[index++] = isFirst ? (subject.mrn == null ? "" : "=\"" + subject.mrn + "\"") : "";
        entries[index++] = isFirst ?
                (subject.mrn == null || subject.mrn.trim().isEmpty() ?
                    "MRN missing. Please update" :
                    (subject.mrnIsInvalid ?
                        "MRN not valid. Please update" :
                            "=\"" + subject.normalizedMrn + "\""
                    )
                ) :
                "";
        entries[index++] = isFirst ? String.valueOf(subject.monthsSinceCreation) + pluralizeIfNeeded(subject.monthsSinceCreation, " month") : "";
        entries[index++] = study != null ? study.studyName : "";
        // FIXME-XH : is this really the local ID?
        entries[index++] = study != null ? String.valueOf(study.localId) : "";
        if (study != null) {
            // remember that if study.monthsSinceAddedToStudy < 0, it means that the date added could not be found in the activity_log
            entries[index++] = study.monthsSinceAddedToStudy >= 0 ?
                    String.valueOf(study.monthsSinceAddedToStudy) + pluralizeIfNeeded(study.monthsSinceAddedToStudy, " month") :
                    "";
        }
        else {
            entries[index++] = "";
        }
        entries[index++] = study != null ? (study.numberOfBookedVisists > 0 ? "Y" : "N") : "N";
        entries[index++] = subject.lastName;
        entries[index++] = subject.firstName;
        entries[index++] = subject.birthdate.toString();
        entries[index++] = subject.gender;
        entries[index++] = subject.streetAddress;
        entries[index++] = subject.city;
        entries[index++] = subject.state;
        entries[index++] = subject.zipcode;
        entries[index++] = subject.country;
        entries[index++] = subject.phone;

        csvWriter.writeNext(entries);

    }

    String pluralizeIfNeeded(int count, String string) {

        return count > 1 ? string + "s" : string;

    }

}
