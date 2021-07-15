package edu.iu.uits.lms.gct.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseGroupWrapper {
   private SerializableGroup allGroup;
   private SerializableGroup teacherGroup;
   private List<SerializableGroup> canvasGroups;

   public void addCanvasGroup(SerializableGroup canvasGroup) {
      if (canvasGroups == null) {
         canvasGroups = new ArrayList<>();
      }
      canvasGroups.add(canvasGroup);
   }

   /**
    * Determines if the required (all and teacher) groups exist
    * @return
    */
   public boolean hasRequiredGroups() {
      return allGroup != null && teacherGroup != null;
   }
}
