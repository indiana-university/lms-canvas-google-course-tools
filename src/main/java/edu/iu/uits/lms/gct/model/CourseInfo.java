package edu.iu.uits.lms.gct.model;

import lombok.Data;

import java.util.List;

@Data
public class CourseInfo {
   private boolean instructor;
   private String allGroupName;
   private String allGroupEmail;
   private String teacherGroupName;
   private String teacherGroupEmail;
   private boolean mailingListEnabled;
   private List<String> teacherRoles;

   private String rootCourseFolder;
   private List<String> optionalCourseFolders;
}
