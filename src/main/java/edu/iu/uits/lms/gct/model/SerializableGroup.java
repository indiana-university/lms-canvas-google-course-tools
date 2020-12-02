package edu.iu.uits.lms.gct.model;

import com.google.api.services.admin.directory.model.Group;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SerializableGroup implements Serializable {

   private String id;
   private String name;
   private String email;

   public SerializableGroup(Group group) {
      this.id = group.getId();
      this.name = group.getName();
      this.email = group.getEmail();
   }
}
