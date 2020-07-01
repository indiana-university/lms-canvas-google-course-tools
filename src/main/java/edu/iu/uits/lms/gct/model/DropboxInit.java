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
import java.util.Date;

@Entity
@Table(name = "GCT_DROPBOX_INIT")
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


   @PreUpdate
   @PrePersist
   public void updateTimeStamps() {
      modifiedOn = new Date();
      if (createdOn==null) {
         createdOn = new Date();
      }
   }
}
