package edu.iu.uits.lms.gct.model;

import lombok.Data;

@Data
public class NotificationData {

   private SerializableGroup allGroup;
   private SerializableGroup teacherGroup;
   private String rootCourseFolder;
   private String courseFilesFolder;
   private String instructorFilesFolder;
   private String groupsFolder;
   private String dropboxFilesFolder;
   private String fileRepositoryFolder;
   private String courseId;
   private String courseTitle;
   private String mailingListAddress;
   private String mailingListName;
}
