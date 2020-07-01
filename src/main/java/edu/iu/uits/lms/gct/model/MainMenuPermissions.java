package edu.iu.uits.lms.gct.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
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

    // if ALL the display criteria is false, display the incomplete warning
    public boolean displaySetupIncompleteWarning() {
        return !(displaySetup || displaySyncCourseRoster || displayDiscussInGoogleGroups ||
                displayShareAndCollaborate || displayFolderWrapper || displayCourseFilesFolder || displayDropBoxFolder ||
                displayMyDropBoxFolder || displayFileRepository || displayInstructorFilesFolder || displayCourseInformation);
    }
}
