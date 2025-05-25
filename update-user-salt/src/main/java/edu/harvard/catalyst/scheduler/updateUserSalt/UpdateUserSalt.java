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
package edu.harvard.catalyst.scheduler.updateUserSalt;

import edu.harvard.catalyst.scheduler.util.BASE64Decoder;
import edu.harvard.catalyst.scheduler.util.OneWayPasswordEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.security.Key;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class UpdateUserSalt {

    private static final Logger log = Logger.getLogger(UpdateUserSalt.class);

    private final DataSource dataSource;

    @Autowired
    public UpdateUserSalt(DataSource dataSource,
                        @Qualifier(value = "encryptionKeyUpdateUserSalt") Key key) {

        this.dataSource = dataSource;
    }

    static long startTime;
    static long endTime;

    public static void main(String[] args) throws SQLException {

        java.util.Date startDate = new java.util.Date();
        startTime = startDate.getTime();
        System.out.println("START UpdateUserSalt at " + startDate.toString());

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-populate-update-user-salt.xml");

        UpdateUserSalt updateUserSalt = (UpdateUserSalt) context.getBean("updateUserSalt");

        updateUserSalt.update();

        java.util.Date endDate = new java.util.Date();
        endTime = endDate.getTime();

        long elapsedTime = endTime - startTime;

        System.out.println("Elapsed time: " + elapsedTime / 1000 + " seconds");
        System.out.println("END UpdateUserSalt at " + endDate.toString());

    }

    void update() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            Statement queryStatement = connection.createStatement();
            Statement updateStatement = connection.createStatement();
            queryStatement.executeQuery("SET AUTOCOMMIT = 0");
            updateStatement.executeQuery("SET AUTOCOMMIT = 1");

            ResultSet resultSet = queryStatement.executeQuery("select id, salt from user");
            while (resultSet != null && resultSet.next()) {
                int rowId = resultSet.getInt(1);
                String oldSalt = resultSet.getString(2);

                byte[] oldBytes = BASE64Decoder.base64DecodeOld(oldSalt);
                String convertedSalt = OneWayPasswordEncoder.getInstance().byteToBase64(oldBytes);

                assert(!oldSalt.equals(convertedSalt));

                String updateString =
                        "update user set salt = '" + convertedSalt +
                                "' where id=" + rowId;

                updateStatement.executeUpdate(updateString);
            }

        }
    }
}
