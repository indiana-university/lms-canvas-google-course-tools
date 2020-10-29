package edu.iu.uits.lms.gct.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RosterSyncCourseData implements Serializable {
   private String courseId;
   private String courseTitle;
   private String allGroupEmail;
   private String teacherGroupEmail;
}
