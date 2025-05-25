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

import com.opencsv.CSVReader;
import edu.harvard.catalyst.scheduler.core.SchedulerRuntimeException;
import edu.harvard.catalyst.scheduler.entity.ArchivalStatus;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
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

import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.kohsuke.args4j.ExampleMode.ALL;


/**
 * MAJOR ASSUMPTIONS:
 *   there is one and only one MRN for each subject
 *   the subject.archival_status column exists
 */

@Component
class SubjectDataDeleter {


    public static final String DELETED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_DELETER = "Deleted by subject " +
                                                                                               "data clean-up program" +
                                                                                               " (SubjectDataDeleter)";
    /**
     * The DAO
     */
    protected final SubjectDAO subjectDAO;
    protected final StandaloneSubjectService standaloneSubjectService;


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

    @Argument(index = 0, required = true, usage = "the input file, containing a single MRN per line (with NO spaces)")
    String inputFileName;

    /**
     * Today's date. It will be set only once when the main class is instantiated. All queries and other comparisons
     * which rely on today's date will use this date, to avoid aberrations
     */
    final Date today;
    final String todayString;
    final long todayMilliseconds;

    boolean decrypt = true;


    /**
     * Most likely a log4j logger (depending on the configuration). Very useful in concatenation mode, so
     * that we can see the history of what the user has done with the program.
     */
    static final Logger log = Logger.getLogger(SubjectDataDeleter.class);

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
        LogManager.getLogger(SubjectDataDeleter.class).setLevel(Level.INFO);

        try {
            Date startDate = new Date();

            // Create bean
            ApplicationContext context = new ClassPathXmlApplicationContext("spring-subject-data-cleaner.xml");
            SubjectDataDeleter subjectDataCleaner = (SubjectDataDeleter) context.getBean("subjectDataDeleter");

            // Note, this could be done in the SubjectDataCleaner constructor if we can get the bean factory to
            // invoke the SubjectDataCleaner constructor with the args passed to it. I tried a version
            // of getBean() which is able to pass arguments that way, but could not get it to work.
            subjectDataCleaner.setUp(args);
            subjectDataCleaner.readCsvAndDeleteSubjects();

            Date endDate = new Date();
            long elapsed = endDate.getTime() - startDate.getTime();
            log.info("Executed in " + elapsed / 1000 + " seconds");

        }
        catch (Exception e) {
            log.error("The following exception/error was thrown:\n", e);
        }

    }


    /**
     * The main class constructor!
     *
     * Spring injects the SubjectDAO and the encryption key.
     * The constructor then makes a record of today's date (which is final, so
     * must be done in the constructor) and the encryption key,
     * for later use.
     *
     * @param subjectDAO
     * @param key
     */
    @Autowired
    SubjectDataDeleter(@Qualifier(value = "encryptionKeyForSubjectDataCleanUp") final Key key,
                       final SubjectDAO subjectDAO,
                       final StandaloneSubjectService standaloneSubjectService) {

        // save the DAO
        this.subjectDAO = subjectDAO;
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
        log.info("Running Subject Data Cleaner on " + this.todayString + " with the following input parameters:");
        log.info("decrypt: " + this.decrypt);
        log.info("input file: " + this.inputFileName);

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
            log.error(e.getMessage());
            printUsageAndExampleCommandLine(parser);
            System.exit(-1);
        }

        // process the command line options which have an impact on the subjectDataCleaner object
        this.decrypt = !this.noDecrypt;

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
    void readCsvAndDeleteSubjects() throws SQLException, IOException {

        CSVReader csvReader = new CSVReader(new FileReader(this.inputFileName));
        String [] nextLine;
        int rowNumber = 0;
        while ((nextLine = csvReader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            rowNumber ++;
            if (nextLine.length != 1) {
                log.error("CSV input row number " + rowNumber + " has " + nextLine.length + " values instead of 1: " + String.join(", ", nextLine));
                continue;
            }
            String idString = nextLine[0];
            int id;
            try {
                id = Integer.parseInt(idString.trim());
                deleteSubject(id, rowNumber);
            }
            catch (NumberFormatException e) {
                log.error("Cannot parse subject ID " + idString + " at line " + rowNumber);
            }
        }

        log.info("");
        csvReader.close();

    }

    public void deleteSubject(int subjectId, final int rowNumber) {

        final Subject subject = subjectDAO.findById(Subject.class, subjectId);
        if (null == subject) {
            log.error("  At line " + rowNumber + " the following subject ID has no match (it may have been previously deleted): " + subjectId );
        }
        else {
            String mrn = "none found";
            Set<SubjectMrn> mrns = subject.getSubjectMrnSet();
            Iterator<SubjectMrn> iterator = mrns.iterator();
            if (iterator.hasNext()) {
                mrn = iterator.next().getMrn();
                if (decrypt) {
                    mrn = SubjectDataEncryptor.decrypt(mrn);
                }
                if (iterator.hasNext()) {
                    log.error("Subject with ID " + subjectId + " has multiple MRNs, therefore it was NOT DELETED");
                    return;
                }
            }
            standaloneSubjectService.markArchivalStatus(
                    subject,
                    DELETED_BY_SUBJECT_DATA_CLEAN_UP_PROGRAM_SUBJECT_DATA_DELETER,
                    "Subject ID: " + subjectId + " (MRN: " + mrn + ") archived (DELETED)",
                     ArchivalStatus.DELETED);
            log.info("Archived (deleted) subject with ID " + subjectId + " (MRN: " + mrn + ")");

            // FIXME-XH: do we need to delete the subjectMrn for this subject (if there is one)?

        }

    }

}
