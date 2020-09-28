package edu.iu.uits.lms.gct.model;

import edu.iu.uits.lms.gct.Constants.FOLDER_TYPES;
import edu.iu.uits.lms.gct.Constants.GROUP_ROLES;
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
   private final List<GROUP_ROLES> possibleFolderOptions = Arrays.asList(GROUP_ROLES.writer, GROUP_ROLES.reader);
   private final List<GROUP_ROLES> possibleFileOptions = Arrays.asList(GROUP_ROLES.commenter, GROUP_ROLES.writer, GROUP_ROLES.reader);
}
