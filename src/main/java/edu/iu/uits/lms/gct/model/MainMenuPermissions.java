package edu.iu.uits.lms.gct.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MainMenuPermissions {
    private boolean displaySetup;
    private boolean displaySyncCourseRoster;
    private boolean displayDiscussInGoogleGroups;
    private boolean displayShareAndCollaborate;
    private boolean displayFolderWrapper;
    private boolean displayCourseFilesFolder;
    private boolean displayDropBoxFolder;
    private boolean displayMyDropBoxFolder;
    private boolean displayFileRepository;
    private boolean displayInstructorFilesFolder;
    private boolean displayCourseInformation;
    private boolean displayUserIneligibleWarning;
    private boolean displayBadCourseTitleWarning;
    private String userIneligibleWarningText;

    // if ALL the display criteria is false, display the incomplete warning
    public boolean displaySetupIncompleteWarning() {
        return !(displaySetup || displaySyncCourseRoster || displayDiscussInGoogleGroups ||
              displayShareAndCollaborate || displayFolderWrapper || displayCourseFilesFolder || displayDropBoxFolder ||
              displayMyDropBoxFolder || displayFileRepository || displayInstructorFilesFolder ||
              displayCourseInformation || displayUserIneligibleWarning || displayBadCourseTitleWarning);
    }
}
