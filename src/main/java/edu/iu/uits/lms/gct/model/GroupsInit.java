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
@Table(name = "GCT_GROUPS_INIT",
      uniqueConstraints = @UniqueConstraint(name = "UK_GCT_GROUPS_INIT", columnNames = {"canvas_course_id", "canvas_group_id", "env"}))
@SequenceGenerator(name = "GCT_GROUPS_INIT_ID_SEQ", sequenceName = "GCT_GROUPS_INIT_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupsInit {

   @Id
   @GeneratedValue(generator = "GCT_GROUPS_INIT_ID_SEQ")
   private Long id;

   @Column(name = "CANVAS_COURSE_ID")
   private String canvasCourseId;

   @Column(name = "CANVAS_GROUP_ID")
   private String canvasGroupId;

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
      if (createdOn == null) {
         createdOn = new Date();
      }
   }
}
