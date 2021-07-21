package edu.iu.uits.lms.gct.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseGroupWrapper implements Serializable {
   private SerializableGroup allGroup;
   private SerializableGroup teacherGroup;
   private List<SerializableGroup> canvasGroups = new ArrayList<>();

   public void addCanvasGroup(SerializableGroup canvasGroup) {
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
