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
package edu.harvard.catalyst.scheduler.batchSubjects;

import edu.harvard.catalyst.scheduler.entity.BaseEntity;
import edu.harvard.catalyst.scheduler.entity.NightlyBatchChanges;
import edu.harvard.catalyst.scheduler.entity.Subject;
import edu.harvard.catalyst.scheduler.persistence.NightlyDAO;
import edu.harvard.catalyst.scheduler.persistence.SiteDAO;
import edu.harvard.catalyst.scheduler.persistence.SubjectDAO;
import edu.harvard.catalyst.scheduler.service.EpicSubjectService;
import edu.harvard.catalyst.scheduler.util.SubjectDataEncryptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.sql.*;
import java.util.List;

/**
 * @author Carl Woolf
 * @link http://cbmi.med.harvard.edu
 */
@Component
public class BatchDumpChanges {

    private static final Logger LOG = Logger.getLogger(BatchDumpChanges.class);

    private final NightlyDAO nightlyDAO;

    @Autowired
    public BatchDumpChanges(NightlyDAO nightlyDAO,
                            @Qualifier(value = "encryptionKeyBatch") final Key key) {

        this.nightlyDAO = nightlyDAO;

        SubjectDataEncryptor.setEncryptionKey(key);
    }


    public static void main(String[] args) throws SQLException {

        ApplicationContext context = new ClassPathXmlApplicationContext("epic-dump-batch-changes.xml");

        BatchDumpChanges batchDumpChanges = (BatchDumpChanges) context.getBean("batchDumpChanges");

        Integer startRecord = Integer.valueOf(args[0]);

        batchDumpChanges.run(startRecord);

    }

    void run(Integer start) throws SQLException {

        LOG.info("\n===========================================" +
                "============================================");

        List<NightlyBatchChanges> nightlyChanges = nightlyDAO.findAllChangeRecords(start);

        LOG.info("Found " + nightlyChanges.size() + " Records of Change, starting with record " + start);

        for (NightlyBatchChanges changes : nightlyChanges) {
            System.out.println("Change " + changes.getId() + ": " +
                    SubjectDataEncryptor.decrypt(changes.getChanges()));
        }

        LOG.info("\n======== Output in console ===================================" +
                "============================================");
    }
}
