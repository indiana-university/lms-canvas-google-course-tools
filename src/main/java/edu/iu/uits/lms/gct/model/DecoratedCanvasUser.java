package edu.iu.uits.lms.gct.model;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.canvas.helpers.EnrollmentHelper;
import edu.iu.uits.lms.canvas.model.Enrollment;
import edu.iu.uits.lms.canvas.model.User;
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