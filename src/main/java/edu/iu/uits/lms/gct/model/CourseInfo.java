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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseInfo {
   private boolean instructor;
   private GroupDetails allGroupDetails;
   private GroupDetails teacherGroupDetails;
   private boolean mailingListEnabled;
   private List<String> teacherRoles;
   private List<GroupDetails> canvasCourseGroups = new ArrayList<>();

   private String rootCourseFolder;
   private List<String> optionalCourseFolders;

   public void addCanvasCourseGroup(GroupDetails canvasCourseGroup) {
      canvasCourseGroups.add(canvasCourseGroup);
   }

   @Data
   @NoArgsConstructor
   public static class GroupDetails implements Serializable {
      private String name;
      private String email;
      private String url;

      public GroupDetails(SerializableGroup group, String url) {
         name = group.getName();
         email = group.getEmail();
         this.url = url;
      }
   }

   @EqualsAndHashCode(callSuper = true)
   @Data
   @NoArgsConstructor
   public static class CanvasGroupDetails extends GroupDetails {
      private boolean existsInCanvas;

      public CanvasGroupDetails(SerializableGroup group, String url, boolean existsInCanvas) {
         super(group, url);
         this.existsInCanvas = existsInCanvas;
      }
   }
}
