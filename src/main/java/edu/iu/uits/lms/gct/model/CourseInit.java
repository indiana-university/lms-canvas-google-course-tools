package edu.iu.uits.lms.gct.model;

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

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(name = "GCT_COURSE_INIT",
      uniqueConstraints = @UniqueConstraint(name = "UK_GCT_COURSE_INIT", columnNames = {"course_id", "env"}))
@SequenceGenerator(name = "GCT_COURSE_INIT_ID_SEQ", sequenceName = "GCT_COURSE_INIT_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseInit {

   public enum SYNC_STATUS {
      ACTIVE,
      INACTIVE
   }

   @Id
   @GeneratedValue(generator = "GCT_COURSE_INIT_ID_SEQ")
   private Long id;

   @Column(name = "COURSE_ID")
   private String courseId;

   @Column(name = "SIS_COURSE_ID")
   private String sisCourseId;

   @Column(name = "COURSE_CODE")
   private String courseCode;

   @Column(name = "TA_TEACHER")
   private boolean taTeacher;

   @Column(name = "DE_TEACHER")
   private boolean deTeacher;

   @Column(name = "COURSE_FOLDER_ID")
   private String courseFolderId;

   @Column(name = "COURSEFILES_FOLDER_ID")
   private String coursefilesFolderId;

   @Column(name = "INSTRUCTOR_FOLDER_ID")
   private String instructorFolderId;

   @Column(name = "DROPBOX_FOLDER_ID")
   private String dropboxFolderId;

   @Column(name = "FILE_REPO_ID")
   private String fileRepoId;

   @Column(name = "GROUPS_FOLDER_ID")
   private String groupsFolderId;

   @Column(name = "MAILING_LIST_ADDRESS")
   private String mailingListAddress;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "CREATED")
   private Date createdOn;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "MODIFIED")
   private Date modifiedOn;

   @Column(name = "ENV", length = 5)
   private String env;

   @Enumerated(EnumType.STRING)
   @Column(name = "SYNC_STATUS")
   private SYNC_STATUS syncStatus = SYNC_STATUS.ACTIVE;

   @PreUpdate
   @PrePersist
   public void updateTimeStamps() {
      modifiedOn = new Date();
      if (createdOn==null) {
         createdOn = new Date();
      }
   }
}
