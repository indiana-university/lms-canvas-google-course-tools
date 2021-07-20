package edu.iu.uits.lms.gct.model;

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
