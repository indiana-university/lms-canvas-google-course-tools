package edu.iu.uits.lms.gct.services;

import edu.iu.uits.lms.gct.model.CourseInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MainMenuPermissionsService {
    @Autowired
    private GoogleCourseToolsService googleCourseToolsService;

    public boolean displaySetup(boolean isInstructor) {
        return isInstructor;
    }

    public boolean displaySyncCourseRoster(boolean isInstructor) {
        return isInstructor;
    }

    public boolean displayDiscussInGoogleGroups(String mailingListAddress) {
        return mailingListAddress != null;
    }

    // Only visible after at least one of the following folders has been created in Setup and is visible to the current role:
    // Instructor: Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if added to instructor group:  Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if not added to instructor group: Course Repo
    // Students: Course Repo, My Drop Box
    // Observers: Do not display to observers
    public boolean displayShareAndCollaborate(boolean isInstructor, boolean isTa, boolean isDesigner, boolean isStudent, CourseInit courseInit, String loginId) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else {
            if (isInstructor && (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                    courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null)) {
                return true;
            } else if (isTa || isDesigner) {
                if ((isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
                    if (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                            courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null) {
                        return true;
                    }
                } else {
                    if (courseInit.getFileRepoId() != null) {
                        return true;
                    }
                }
            } else if (isStudent) {
                if (googleCourseToolsService.getDropboxInit(courseInit.getCourseId(), loginId) != null || courseInit.getFileRepoId() != null) {
                    return true;
                }
            }
        }
        // if we made it here, return false
        return false;
    }

    // Only visible after at least one of the following folders has been created in Setup and is visible to the current role:
    // Instructor: Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if added to instructor group:  Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if not added to instructor group: Course Files, Course Repo
    // Students: Course Files, Course Repo, My Drop Box
    // Observers: Course Files, Course Repo
    public boolean displayFolderWrapper(boolean isInstructor, boolean isTa, boolean isDesigner, boolean isStudent, boolean isObserver, CourseInit courseInit, String loginId) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else {
            if (isInstructor && (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                    courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null)) {
                return true;
            } else if (isTa || isDesigner) {
                if ((isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
                    if (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                            courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null) {
                        return true;
                    }
                } else {
                    if (courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null) {
                        return true;
                    }
                }
            } else if (isStudent) {
                if (googleCourseToolsService.getDropboxInit(courseInit.getCourseId(), loginId) != null || courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null) {
                    return true;
                }
            } else if (isObserver) {
                if (courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null) {
                    return true;
                }
            }
        }

        // if we made it here, return false
        return false;
    }

    public boolean displayCourseFilesFolder(String coursefilesFolderId) {
        return coursefilesFolderId != null;
    }

    // Only visible if Drop Boxes have been created in Setup.
    // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
    public boolean displayDropBoxFolder(boolean isInstructor, boolean isTa, boolean isDesigner, CourseInit courseInit) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else if (courseInit.getDropboxFolderId() != null) {
            if (isInstructor || (isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
                return true;
            }
        }
        return false;
    }

    public boolean displayMyDropBoxFolder(boolean isStudent, String courseId, String loginId) {
        return isStudent && googleCourseToolsService.getDropboxInit(courseId, loginId) != null;
    }

    public boolean displayFileRepository(String fileRepoId) {
        return fileRepoId != null;
    }

    // Only visible if Instructor Files folder has been created in Setup.
    // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
    public boolean displayInstructorFilesFolder(boolean isInstructor, boolean isTa, boolean isDesigner, CourseInit courseInit) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else if (courseInit.getInstructorFolderId() != null) {
            if (isInstructor || (isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
                return true;
            }
        }
        return false;
    }

    // Only visible after a folder or mailing list has been created in Setup.
    public boolean displayCourseInformation(CourseInit courseInit) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else if (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null ||
                courseInit.getMailingListAddress() != null) {
            return true;
        }
        return false;
    }
}
