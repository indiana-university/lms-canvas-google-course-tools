package edu.iu.uits.lms.gct.mailinglist;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class MxRecordService {

   @Autowired
   @Qualifier("sqlServerDb")
   private DataSource dataSource;

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   public static final String SELECT_COLUMNS = "Username, ExternalEmailAddress";
   public static final String DATE_CREATED = "DateCreated";
   public static final String USERNAME = "Username";
   public static final String EXTERNAL_EMAIL_ADDRESS = "ExternalEmailAddress";
   public static final String INSERT_COLUMNS = "Username, ExternalEmailAddress, ExpDate, DateCreated, CreatedBy, CreateMethod, Status, Comment";

   // use this to do both the check for an existing record and to create a new record
   public boolean didSetupMailingList(String usernameForMxRecord) {
      boolean success = false;

      // check if an address already exists
      MxRecord existingMxRecord = null;
      try {
         existingMxRecord = getMxRecord(usernameForMxRecord);
      } catch (SQLException e) {
         log.error("Error looking up MxRecord for " + usernameForMxRecord, e);
      }
      if (existingMxRecord != null && MxRecord.RESULT_SUCCESS.equals(existingMxRecord.getResult())) {
         // found an existing record
         success = true;
      } else {
         MxRecord newMxRecord = null;
         try {
            newMxRecord = createMxRecord(usernameForMxRecord);
         } catch (SQLException e) {
            log.error("Error creating new MxRecord for " + usernameForMxRecord, e);
         }
         if (newMxRecord != null && MxRecord.RESULT_SUCCESS.equals(newMxRecord.getResult())) {
            success = true;
         }
      }

      return success;
   }

   public MxRecord getMxRecord(String username) throws SQLException {
      String sql = "select " + SELECT_COLUMNS + " from UITS_EMA_MailSecurity.dbo.MailContact where " + USERNAME + " = ? order by " + DATE_CREATED + " desc";
      Connection conn = dataSource.getConnection();
      PreparedStatement preparedStatement = null;
      ResultSet rs = null;
      MxRecord result = new MxRecord();

      try {
         preparedStatement = conn.prepareStatement(sql);
         preparedStatement.setString(1, username);
         rs = preparedStatement.executeQuery();

         if (rs.next()) {
            result.setUsername(rs.getString(USERNAME));
            result.setExternalEmailAddress(rs.getString(EXTERNAL_EMAIL_ADDRESS));
         }
      } catch (SQLException e) {
         log.error("uh oh", e);
      } finally {
         // close every thing
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException sqle) {
            log.error("Error closing resultset ", sqle);
         }

         try {
            if (preparedStatement != null) {
               preparedStatement.close();
            }
         } catch (SQLException sqle) {
            log.error("Error closing statement ", sqle);
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException sqle) {
            log.error("Error closing connection ", sqle);
         }
      }

      if (result.getExternalEmailAddress() != null && !result.getExternalEmailAddress().isEmpty()) {
         result.setResult(MxRecord.RESULT_SUCCESS);
      } else {
         result.setResult(MxRecord.RESULT_FAILED);
      }

      return result;
   }

   public MxRecord createMxRecord(String username) throws SQLException {
      String sql = "insert into UITS_EMA_MailSecurity.dbo.MailContact (" + INSERT_COLUMNS + ") values (?, ?, ?, ?, ?, ?, ?, ?)";
      Connection conn = dataSource.getConnection();
      PreparedStatement preparedStatement = null;

      MxRecord newMxRecord = new MxRecord();
      String email = username + "@xmail.iu.edu";
      // current date stuff
      Date createdDate = new Date();
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      String stringCreatedDate = dateFormat.format(createdDate);

      String expirationDate = "2099-12-31";
      String createdBy = "edsdev";
      String createMethod = "UITSLT";
      String status = "Pending";
      String comment = "Created by Google Course Tools app";

      boolean recordCreated = false;

      try {
         preparedStatement = conn.prepareStatement(sql);
         preparedStatement.setString(1, username);
         preparedStatement.setString(2, email);
         preparedStatement.setString(3, expirationDate);
         preparedStatement.setString(4, stringCreatedDate);
         preparedStatement.setString(5, createdBy);
         preparedStatement.setString(6, createMethod);
         preparedStatement.setString(7, status);
         preparedStatement.setString(8, comment);
         preparedStatement.execute();

         recordCreated = true;

      } catch (SQLException e) {
         log.error("uh oh", e);
      } finally {
         // close every thing
         try {
            if (preparedStatement != null) {
               preparedStatement.close();
            }
         } catch (SQLException sqle) {
            log.error("Error closing statement ", sqle);
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException sqle) {
            log.error("Error closing connection ", sqle);
         }
      }

      if (recordCreated) {
         newMxRecord.setResult(MxRecord.RESULT_SUCCESS);
      } else {
         newMxRecord.setResult(MxRecord.RESULT_FAILED);
      }

      return newMxRecord;
   }

}
