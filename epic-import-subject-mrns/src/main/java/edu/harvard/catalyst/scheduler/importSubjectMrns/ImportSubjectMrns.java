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
package edu.harvard.catalyst.scheduler.importSubjectMrns;

import com.opencsv.CSVReader;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.entity.SubjectMrn;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Carl Woolf
 * @link http://cbmi.med.harvard.edu
 */
@Component
public class ImportSubjectMrns {

    private static final Logger LOG = Logger.getLogger(ImportSubjectMrns.class);

    private final SubjectDAO subjectDAO;

    private int idIndex;
    private int mrnIndex;
    private int siteIndex;
    private int statusIndex;
    private int puidIndex;

    private boolean clobberMrns;

    @Autowired
    public ImportSubjectMrns(SubjectDAO subjectDAO,
                             @Qualifier(value = "encryptionKeyBatch") final Key key) {

        this.subjectDAO = subjectDAO;
        SubjectDataEncryptor.setEncryptionKey(key);
    }


    // scheduler.properties also needs to be on path!
    public static void main(String[] args) throws SQLException, IOException {

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-epic-import-subject-mrns.xml");

        ImportSubjectMrns importSubjectMrns = (ImportSubjectMrns)context.getBean("importSubjectMrns");

        if (args.length != 7) {
            System.err.println("Usage: ImportSubjectMrns <path-to-csv> " +
                    "<idIndex(zero-based)> <mrnIndex> <siteIndex> <statusIndex> <puidIndex> <true|false");
        }
        String csvFilename = args[0];
        importSubjectMrns.clobberMrns = Boolean.parseBoolean(args[6]);

        importSubjectMrns.loadIndices(
                Integer.valueOf(args[1]),
                Integer.valueOf(args[2]),
                Integer.valueOf(args[3]),
                Integer.valueOf(args[4]),
                Integer.valueOf(args[5]));

        CSVReader csvReader = new CSVReader(new FileReader(csvFilename));

        importSubjectMrns.loopThroughCsv(csvReader);
    }

    void loopThroughCsv(CSVReader csvReader) throws SQLException, IOException {

        String [] nextLine;
        int rowNumber = -1;
        int numSuccess = 0;

        LOG.info("T/F: We will clobber DB MRNs with CSV values? " + clobberMrns);
        while ((nextLine = csvReader.readNext()) != null) {

            rowNumber++;
            LOG.info("Processing row: " + rowNumber);

            int maxIndex = getMaxIndex();
            if (nextLine.length <= maxIndex) {
                LOG.error("CSV input row number " + rowNumber +
                        " has " + nextLine.length +
                        " values instead of at least " +
                        (maxIndex+1) + ": " +
                        String.join(", ", nextLine));
                continue;
            }

            String subjectIdString = nextLine[idIndex].trim();

            try {
                int id = Integer.valueOf(subjectIdString);
                String mrn = delegateEncrypt(nextLine[mrnIndex].trim());
                String site = nextLine[siteIndex].trim();
                String status = nextLine[statusIndex].trim();
                String puid = delegateEncrypt(nextLine[puidIndex].trim());

                Subject subject = subjectDAO.findById(id);
                if (null == subject) {
                    LOG.error("Row: " + rowNumber + ". No subject found with id: " + id);
                    continue;
                }

                Set<SubjectMrn> previousSubjectMrns = subject.getSubjectMrnSet();
                int numPrevious = previousSubjectMrns.size();

                if (numPrevious > 1) {
                    LOG.error("Row: " + rowNumber + ". This subject id: " + subjectIdString + ", has >1 previous MRN!!");
                    continue;
                }
                else {
                    String previousPuid = subject.getPuid();

                    if (numPrevious == 1) {
                        SubjectMrn subjectMrn = previousSubjectMrns.stream().findFirst().get();

                        if (!subjectMrn.getMrn().equals(mrn) && !clobberMrns) {
                            LOG.error("Row: " + rowNumber + ". This subject's MRN-code differs between the DB and the CSV!!");
                            continue;
                        }

                        subjectMrn.setMrn(mrn);
                        subjectMrn.setSite(site);
                        subjectMrn.setStatus(status);
                        subjectDAO.updateEntity(subjectMrn);
                    } else { // zero (AKA < 1)
                        SubjectMrn subjectMrn = new SubjectMrn(subject, mrn, site, status);
                        subjectDAO.createEntity(subjectMrn);
                    }
                    if ( ! puid.equalsIgnoreCase(previousPuid)) {
                        subject.setPuid(puid);
                        subjectDAO.updateEntity(subject);
                    }
                }
            }
            catch (NumberFormatException e) {
                LOG.error("Row: " + rowNumber + ". Invalid subject id: " + subjectIdString);
                continue;
            }

            numSuccess++;
        }
        LOG.info(numSuccess + " successes out of " + ++rowNumber + " data rows");
        csvReader.close();
    }
    String delegateEncrypt(String input) {

        return SubjectDataEncryptor.encrypt(input);
    }

    public int getIdIndex() {
        return idIndex;
    }

    public int getMrnIndex() {
        return mrnIndex;
    }

    public int getSiteIndex() {
        return siteIndex;
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    public void setIdIndex(int idIndex) {
        this.idIndex = idIndex;
    }

    public void setMrnIndex(int mrnIndex) {
        this.mrnIndex = mrnIndex;
    }

    public void setSiteIndex(int siteIndex) {
        this.siteIndex = siteIndex;
    }

    public void setStatusIndex(int statusIndex) {
        this.statusIndex = statusIndex;
    }

    public int getPuidIndex() {
        return puidIndex;
    }

    public void setPuidIndex(int puidIndex) {
        this.puidIndex = puidIndex;
    }

    public void setClobberMrns(boolean clobberMrns) {
        this.clobberMrns = clobberMrns;
    }

    int getMaxIndex() {
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(idIndex);
        treeSet.add(mrnIndex);
        treeSet.add(siteIndex);
        treeSet.add(statusIndex);
        treeSet.add(puidIndex);

        return treeSet.last();
    }
    void loadIndices(int id, int mrn, int site, int status, int puid) {
        idIndex = id;
        mrnIndex = mrn;
        siteIndex = site;
        statusIndex = status;
        puidIndex = puid;
    }
}
