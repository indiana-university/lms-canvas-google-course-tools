package edu.iu.uits.lms.gct.model;

import edu.iu.uits.lms.gct.Constants.FOLDER_TYPES;
import edu.iu.uits.lms.gct.Constants.PERMISSION_ROLES;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedFilePermissionModel {
   private List<SharedFilePermission> sharedFilePermissions;
   private FOLDER_TYPES destFolderType;
   private final List<PERMISSION_ROLES> possibleFolderOptions = Arrays.asList(PERMISSION_ROLES.writer, PERMISSION_ROLES.reader);
   private final List<PERMISSION_ROLES> possibleFileOptions = Arrays.asList(PERMISSION_ROLES.commenter, PERMISSION_ROLES.writer, PERMISSION_ROLES.reader);
}
