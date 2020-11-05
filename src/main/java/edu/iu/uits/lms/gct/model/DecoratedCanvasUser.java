package edu.iu.uits.lms.gct.model;

import canvas.client.generated.model.Enrollment;
import canvas.client.generated.model.User;
import canvas.helpers.EnrollmentHelper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DecoratedCanvasUser {
   private String email;
   private String loginId;
   private List<DecoratedEnrollment> enrollments;

   public DecoratedCanvasUser(User user) {
      this.email = user.getEmail();
      this.loginId = user.getLoginId();
      this.enrollments = user.getEnrollments().stream()
            .map(DecoratedEnrollment::new)
            .collect(Collectors.toList());
   }

   public boolean isTeacher() {
      return hasEnrollment(EnrollmentHelper.TYPE_TEACHER);
   }

   public boolean isStudent() {
      return hasEnrollment(EnrollmentHelper.TYPE_STUDENT);
   }

   public boolean isDesigner() {
      return hasEnrollment(EnrollmentHelper.TYPE_DESIGNER);
   }

   public boolean isTa() {
      return hasEnrollment(EnrollmentHelper.TYPE_TA);
   }

   public boolean isObserver() {
      return hasEnrollment(EnrollmentHelper.TYPE_OBSERVER);
   }

   /**
    * Check to see if the user has this enrollment
    * @param role
    * @return
    */
   private boolean hasEnrollment(String role) {
      return enrollments.stream().anyMatch(e -> role.equals(e.getType()));
   }

   @Data
   @AllArgsConstructor
   private static class DecoratedEnrollment implements Serializable {
      private String type;
      private String role;
      private String enrollmentState;

      public DecoratedEnrollment(Enrollment enrollment) {
         this.type = enrollment.getType();
         this.role = enrollment.getRole();
         this.enrollmentState = enrollment.getEnrollmentState();
      }
   }
}
