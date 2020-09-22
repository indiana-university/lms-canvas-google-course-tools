package edu.iu.uits.lms.gct.model;

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
   private final List<GROUP_ROLES> possibleFolderOptions = Arrays.asList(GROUP_ROLES.EDITOR, GROUP_ROLES.VIEWER);
   private final List<GROUP_ROLES> possibleFileOptions = Arrays.asList(GROUP_ROLES.COMMENTER, GROUP_ROLES.EDITOR, GROUP_ROLES.VIEWER);
}
