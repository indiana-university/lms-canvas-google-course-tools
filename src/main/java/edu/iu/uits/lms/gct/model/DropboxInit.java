package edu.iu.uits.lms.gct.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(name = "GCT_DROPBOX_INIT",
      uniqueConstraints = @UniqueConstraint(name = "UK_GCT_DROPBOX_INIT", columnNames = {"course_id", "env", "login_id"}))
@SequenceGenerator(name = "GCT_DROPBOX_INIT_ID_SEQ", sequenceName = "GCT_DROPBOX_INIT_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DropboxInit {
   @Id
   @GeneratedValue(generator = "GCT_DROPBOX_INIT_ID_SEQ")
   private Long id;

   @Column(name = "LOGIN_ID")
   private String loginId;

   @Column(name = "GOOGLE_LOGIN_ID")
   private String googleLoginId;

   @Column(name = "COURSE_ID")
   private String courseId;

   @Column(name = "FOLDER_ID")
   private String folderId;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "CREATED")
   private Date createdOn;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "MODIFIED")
   private Date modifiedOn;

   @Column(name = "ENV", length = 5)
   private String env;

   @PreUpdate
   @PrePersist
   public void updateTimeStamps() {
      modifiedOn = new Date();
      if (createdOn==null) {
         createdOn = new Date();
      }
   }
}
