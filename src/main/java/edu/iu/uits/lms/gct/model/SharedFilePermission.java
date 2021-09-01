package edu.iu.uits.lms.gct.model;

import com.google.api.services.drive.model.File;
import edu.iu.uits.lms.gct.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedFilePermission {
   @NonNull
   private File file;
   private String allPerm;
   private String teacherPerm;
   private String courseGroupPerm;

   public boolean isFolder() {
      return Constants.FOLDER_MIME_TYPE.equals(file.getMimeType());
   }

   /**
    * This is a bit of a hack, but the best wey I could find to get the 32x32 version of the icon.
    * It replaces the /16/ segment in the url with a /32/.  There is also a /128/ version, but likely too big for our uses!
    * @return
    */
   public String getIconUrl32() {
      return file.getIconLink().replace("/16/", "/32/");
   }

}
