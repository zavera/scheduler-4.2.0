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


package edu.harvard.catalyst.scheduler.subjectDataMerger;

import com.opencsv.CSVReader;
import edu.harvard.catalyst.scheduler.dto.response.MrnInfoDTO;
import edu.harvard.catalyst.scheduler.entity.*;
import edu.harvard.catalyst.scheduler.persistence.AppointmentDAO;
import edu.harvard.catalyst.scheduler.persistence.StudyDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.StandaloneSubjectService;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.kohsuke.args4j.ExampleMode.ALL;


/**
 * MAJOR ASSUMPTIONS:
 *   there is one and only one MRN for each subject
 *   the subject.archival_status column exists
 */

@Component
class DuplicateSubjectDataMerger {

    public static final String MERGED_BY_DUPLICATE_SUBJECT_DATA_MERGER = "Merged by duplicate subject data merger program ";


    /**
     * The DAO
     */
    protected final SubjectDAO subjectDAO;
    protected final StudyDAO studyDAO;
    protected final AppointmentDAO appointmentDao;
    protected final StandaloneSubjectService standaloneSubjectService;

    /*
     * The following few variables are command line options, as per the @Option annotation (args4j)
     */

    /**
     * A command-line option indicating whether to output usage text
     * It is a command-line option
     */
    @Option(name = "-help", required = false, usage = "outputs usage info")
    boolean help = false;

    /**
     * A command-line option indicating whether or not to decrypt the subject data retrieved from the database.
     * It is a command-line option
     */
    @Option(name = "-removeDupStudySubjects", required = false, usage = "Do not allow duplicate study subjects " +
                                                                        "in the case where the duplicate subjects are in the same study." +
                                                                        " In this case duplicate study subjects will be deleted.")
    boolean removeDupStudySubjects = false;

    @Argument(index = 0, required = true, usage = "the input file, containing comma-separated pairs of subject Ids (with NO spaces)")
    String inputFileName;

    /**
     * Today's date. It will be set only once when the main class is instantiated. All queries and other comparisons
     * which rely on today's date will use this date, to avoid aberrations
     */
    final Date today;
    final String todayString;
    final long todayMilliseconds;

    private final DataSource dataSource;


    /**
     * Most likely a log4j logger (depending on the configuration). Very useful in concatenation mode, so
     * that we can see the history of what the user has done with the program.
     */
    static final Logger log = Logger.getLogger(DuplicateSubjectDataMerger.class);

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
        LogManager.getLogger(DuplicateSubjectDataMerger.class).setLevel(Level.INFO);

        try {
            Date startDate = new Date();

            // Create bean
            ApplicationContext context = new ClassPathXmlApplicationContext("spring-subject-data-merger.xml");
            DuplicateSubjectDataMerger duplicateSubjectDataMerger = (DuplicateSubjectDataMerger) context.getBean("duplicateSubjectDataMerger");

            // Note, this could be done in the SubjectDataCleaner constructor if we can get the bean factory to
            // invoke the SubjectDataCleaner constructor with the args passed to it. I tried a version
            // of getBean() which is able to pass arguments that way, but could not get it to work.
            duplicateSubjectDataMerger.setUp(args);
            duplicateSubjectDataMerger.readCsvAndMergeSubjects();

            Date endDate = new Date();
            long elapsed = endDate.getTime() - startDate.getTime();
            duplicateSubjectDataMerger.log.info("Executed in " + elapsed / 1000 + " seconds");

        }
        catch (Exception e) {
            log.error("The following exception/error was thrown:\n", e);
        }

    }


    /**
     * The main class constructor!
     * <p>
     * Spring injects the SubjectDAO and the encryption key.
     * The constructor then makes a record of today's date (which is final, so
     * must be done in the constructor) and the encryption key,
     * for later use.
     *
     * @param subjectDAO
     * @param studyDAO
     * @param appointmentDao
     * @param standaloneSubjectService
     * @param key
     */
    @Autowired
    DuplicateSubjectDataMerger(DataSource dataSource,
            @Qualifier(value = "encryptionKeyForSubjectDataMerger") final Key key,
            final SubjectDAO subjectDAO,
            final StudyDAO studyDAO,
            final AppointmentDAO appointmentDao,
            final StandaloneSubjectService standaloneSubjectService
    ) {

        // save the DAO
        this.dataSource = dataSource;
        this.subjectDAO = subjectDAO;
        this.studyDAO = studyDAO;
        this.appointmentDao = appointmentDao;
        this.standaloneSubjectService = standaloneSubjectService;

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
        this.log.info("Running Subject Data Merger on " + this.todayString + " with the following input parameters:");
        this.log.info("input file: " + this.inputFileName);

    }

    String decrypt(String str) {
        return SubjectDataEncryptor.decrypt(str);
    }

    /**
     * Parse command line arguments and options uing args4j
     * See https://github.com/kohsuke/args4j/blob/master/args4j/examples/SampleMain.java
     * for sample code
     *
     * @param args the same String[] array that is passed to the main static method
     */
    void parseCommandLineAndExitIfNeeded(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        // TODO look at not calling exit() from within if/else blocks
        try {
            // parse the arguments.
            parser.parseArgument(args);
            // show help screen?
            if (this.help) {
                printUsageAndExampleCommandLine(parser);
                System.exit(0);
            }
        }
        catch (CmdLineException e) {
            log.error(e.getMessage());
            printUsageAndExampleCommandLine(parser);
            System.exit(-1);
        }
    }

    void printUsageAndExampleCommandLine(CmdLineParser parser) {
        System.err.println("Usage:");
        System.err.println("  java " + this.getClass().getName() + " [options...]");
        System.err.println("where the options are:");
        // print the list of available options
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
    void readCsvAndMergeSubjects() throws SQLException, IOException {
        try (FileReader fileReader = new FileReader(this.inputFileName); CSVReader csvReader = new CSVReader(fileReader)) {
            String[] nextLine;
            int rowNumber = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                rowNumber++;
                if (nextLine.length != 2) {
                    log.error(
                            "CSV input row number " + rowNumber + " has " + nextLine.length + " values instead of 2: " +
                            String.join(", ", nextLine));
                    continue;
                }
                int secondaryId = Integer.parseInt(nextLine[0].trim());
                int primaryId = Integer.parseInt(nextLine[1].trim());

                if (primaryId == secondaryId) {
                    log.error("At CSV input row number " + rowNumber + ": Cannot merge a subject into itself, ID = " +
                              secondaryId);
                    continue;
                }
                mergeSubject(secondaryId, primaryId, rowNumber);
            }
        }

        log.info("");

    }

    private SubjectMrn findMrnMatchWithLeadingZero(Set<SubjectMrn> primaryMrns, String mrn) {
        SubjectMrn subjectMrn = null;
        String decryptedMrn = decrypt(mrn);

        for (SubjectMrn primaryMrn : primaryMrns) {
            String decryptedPrimaryMrn  = decrypt(primaryMrn.getMrn());
            String mrnMinusLeadingZeros = decryptedPrimaryMrn.replaceAll("^0*", "");

            if (!decryptedMrn.equals(decryptedPrimaryMrn) && mrnMinusLeadingZeros.equals(decryptedMrn)) {
                subjectMrn = primaryMrn;
            }
        }

        return subjectMrn;
    }

    private void disableStudySubjectContraints() throws SQLException{
        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();
            statement.executeQuery("set FOREIGN_KEY_CHECKS=0");
        }

    }

    private void enableStudySubjectContraints() throws SQLException{
        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();
            statement.executeQuery("set FOREIGN_KEY_CHECKS=1");
        }
    }

    public void mergeSubject(final int secondarySubjectId, final int primarySubjectId, final int rowNumber) throws SQLException{

        disableStudySubjectContraints();
        // there should be a single MRN per subject b/c this program is run on standalone data only

        final Subject primarySubject = subjectDAO.findById(primarySubjectId);
        if (null == primarySubject) {
            log.error("  At line " + rowNumber +
                      " the following subject ID has no match: " +
                      primarySubjectId);
            return;
        }
        final Subject secondarySubject = subjectDAO.findById(secondarySubjectId);
        if (null == secondarySubject) {
            log.error("  At line " + rowNumber +
                      " the following subject ID has no match: " +
                      secondarySubjectId);
            return;
        }

        Set<SubjectMrn> primaryMrns = primarySubject.getSubjectMrnSet();
        if (primaryMrns.size() == 0) {
            log.error("At line " + rowNumber + " primary subject with ID " + primarySubjectId +
                      " does not have an MRN. It will be ignored.");
            return;
        }

        final Set<SubjectMrn> secondaryMrns = secondarySubject.getSubjectMrnSet();
        if (secondaryMrns.size() == 0) {
            log.error("At line " + rowNumber + " secondary subject with ID " + secondarySubjectId +
                      " does not have an MRN. It will be ignored.");
            return;
        }

        String primarySubjectDescription = "subject with ID " + primarySubjectId;
        String secondarySubjectDescription = "subject with ID " + secondarySubjectId;

        log.info("Merging " + secondarySubjectDescription + " into " + primarySubjectDescription);

        List<String> changeDetails = new ArrayList<>();
        changeDetails.add(secondarySubjectDescription + " merged into " + primarySubjectDescription);

        // Find all the study_subject entities that refer to the primary Subject.
        // The reason is that we cannot re-map a secondary subject's study_subject rows if
        // the primary subject already has study_subject records for the same studies
        // Instead, when such a collision occurs, we will remap booked_visits to the primary study_subject,
        // and get rid of the secondary study_subject
        final Map<Integer, StudySubject> allPrimaryStudySubjectsMap = new HashMap<>();
        primaryMrns.stream().forEach(primaryMrn -> {
            List<StudySubject> primaryStudySubjects = studyDAO.findStudySubjectBySubjectMrn(primaryMrn);

            Map<Integer, StudySubject> primaryStudySubjectsMap = primaryStudySubjects
                    .stream()
                    .collect(Collectors.toMap(
                            ss -> ss.getStudy().getId(),
                            Function.identity()
                    ));
            allPrimaryStudySubjectsMap.putAll(primaryStudySubjectsMap);
        });

        secondaryMrns.stream().forEach(secondaryMrn -> {
            final SubjectMrn leadingZeroPrimaryMrn = findMrnMatchWithLeadingZero(primaryMrns, secondaryMrn.getMrn());
            // Find the study_subject records for the secondary subject
            List<StudySubject> secondaryStudySubjects = studyDAO.findStudySubjectBySubjectMrn(secondaryMrn);
            boolean duplicateAlreadyFound = false;
            SubjectMrn dupSubjectMrn = null;

            for (StudySubject secondaryStudySubject: secondaryStudySubjects){
                Study mergeStudy = secondaryStudySubject.getStudy();
                final StudySubject dupStudySubject = allPrimaryStudySubjectsMap.get(mergeStudy.getId());

                if (leadingZeroPrimaryMrn != null) {
                    SubjectMrn primaryMrn = leadingZeroPrimaryMrn;
                    List<BookedVisit> bookedVisits = appointmentDao.getAllBookedVisitByStudyAndSubjectMrn(mergeStudy, secondaryMrn);
                    remapBookedVisits(bookedVisits, primaryMrn, changeDetails);

                    log.info("Found mrn with leading zero for subject id: " + secondaryStudySubject.getSubject().getId());

                    if(dupStudySubject != null){
                        log.info("  Removing subject_study record for study '" +
                                 secondaryStudySubject.getStudy().getName() + "' with id '" + secondaryStudySubject.getId());
                        changeDetails.add("StudySubject#" + secondaryStudySubject.getId() + ":DELETED");

                        studyDAO.deleteEntity(secondaryStudySubject);
                    }
                    else{
                        changeDetails.add("StudySubject#" + secondaryStudySubject.getId() + ": subjectMrn: from#"
                                          + secondaryMrn.getId() + ":to#" +  primaryMrn.getId() );

                        secondaryStudySubject.setSubjectMrn(primaryMrn);
                        studyDAO.updateEntity(secondaryStudySubject);
                    }
                } else {
                    //check if the primary and secondary mrns are in the same study
                    //or there are duplicate primary and secondary mrns
                    MrnInfoDTO mrnInfoDTO = new MrnInfoDTO(secondaryMrn.getMrn());
                    mrnInfoDTO.setInstitution(secondaryMrn.getSite());

                    if(dupSubjectMrn == null) {
                        SubjectMrn subjectMrn = new SubjectMrn();
                        subjectMrn.setMrn(SubjectDataEncryptor.decrypt(secondaryMrn.getMrn()));
                        subjectMrn.setSite(secondaryMrn.getSite());
                        subjectMrn.setSubject(primarySubject);
                        dupSubjectMrn = subjectDAO.getSubjectMrnForSubject(subjectMrn);
                    }

                    if(dupSubjectMrn != null){
                        if(!duplicateAlreadyFound) {
                            log.info("Found duplicate MRNs for subject: " + secondaryStudySubject.getSubject().getId());
                        }

                        //remap any booked visits in the case of duplicate subject mrns
                        List<BookedVisit> bookedVisits = appointmentDao.getAllBookedVisitByStudyAndSubjectMrn(mergeStudy, secondaryMrn);
                        remapBookedVisits(bookedVisits, dupSubjectMrn, changeDetails);

                        duplicateAlreadyFound = true;

                        if(dupStudySubject == null) {
                            secondaryStudySubject.setSubjectMrn(dupSubjectMrn);
                            changeDetails.add("StudySubject#" + secondaryStudySubject.getId() + ".subjectMrn:from#"
                                              + secondaryMrn.getId() + ":to#" +  dupSubjectMrn.getId() );

                            subjectDAO.updateEntity(secondaryStudySubject);
                        }else{
                            log.info("  Removing subject_study record for study '" +
                                     secondaryStudySubject.getStudy().getName() + "' with id '" + secondaryStudySubject.getId());
                            changeDetails.add("StudySubject#" + secondaryStudySubject.getId() + ":DELETED");
                            studyDAO.deleteEntity(secondaryStudySubject);
                        }
                    }


                    SubjectMrn primaryMrn = null;

                    if(dupStudySubject != null) {
                        primaryMrn = dupStudySubject.getSubjectMrn();
                    }

                    if (primaryMrn != null) {
                        log.info("Found same subject in study "  + secondaryStudySubject.getStudy().getName() + " for subject: " + secondaryStudySubject.getSubject().getId());

                        if(this.removeDupStudySubjects) {
                            log.info("  Removing subject_study record for study '" +
                                 secondaryStudySubject.getStudy().getName() + "' with id '" + secondaryStudySubject.getId());
                            changeDetails.add("StudySubject#" + secondaryStudySubject.getId() + ":DELETED");

                            studyDAO.deleteEntity(secondaryStudySubject);
                        }
                    }
                }
            }

            if(dupSubjectMrn == null && leadingZeroPrimaryMrn == null) {
                changeDetails.add("SubjectMrn#" + secondaryMrn.getId() + ".subject:from#"
                                  + secondaryMrn.getSubject().getId() + ":to#" +  primarySubject.getId() );

                secondaryMrn.setSubject(primarySubject);
                subjectDAO.updateEntity(secondaryMrn);
            }
        });

        log.info("  Archived (MERGED) subject with id " + secondarySubject.getId());
        standaloneSubjectService.markArchivalStatus(secondarySubject,
                                                    MERGED_BY_DUPLICATE_SUBJECT_DATA_MERGER,
                                                    String.join(",", changeDetails), ArchivalStatus.MERGED
        );

        enableStudySubjectContraints();
    }

    public void remapBookedVisits(List<BookedVisit> bookedVisits, SubjectMrn primarySubjectMrn, List<String> changeDetails) {
        bookedVisits.stream().forEach(bookedVisit -> {
            changeDetails.add("BookedVisit#" + bookedVisit.getId() + ".subjectMrn:from#"  + bookedVisit.getSubjectMrn().getMrn() + ":to#" + primarySubjectMrn.getMrn());
            // no need to change the study
            bookedVisit.setSubjectMrn(primarySubjectMrn);
            appointmentDao.updateEntity(bookedVisit);
        });
    }
}
