package edu.iu.uits.lms.gct.model;

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
