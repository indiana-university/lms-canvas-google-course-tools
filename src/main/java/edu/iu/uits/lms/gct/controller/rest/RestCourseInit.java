package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.CourseInit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestCourseInit extends CourseInit {
   private Boolean deTeacher;
   private Boolean taTeacher;
}
