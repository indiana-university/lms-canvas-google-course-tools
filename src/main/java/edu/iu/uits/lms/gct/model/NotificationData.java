package edu.iu.uits.lms.gct.model;

import com.google.api.services.admin.directory.model.Group;
import lombok.Data;

@Data
public class NotificationData {

   private Group allGroup;
   private Group teacherGroup;
   private String rootCourseFolder;
   private String courseFilesFolder;
   private String instructorFilesFolder;
   private String dropboxFilesFolder;
   private String fileRepositoryFolder;
   private String courseId;
   private String courseTitle;
   private String mailingListAddress;
   private String mailingListName;
}
