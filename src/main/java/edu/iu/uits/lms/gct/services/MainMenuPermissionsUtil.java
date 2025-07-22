package edu.iu.uits.lms.gct.services;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainMenuPermissionsUtil {

    public static boolean displaySetup(boolean isInstructor) {
        return isInstructor;
    }

    public static boolean displaySyncCourseRoster(boolean isInstructor) {
        return isInstructor;
    }

    public static boolean displayDiscussInGoogleGroups(String mailingListAddress) {
        return mailingListAddress != null;
    }

    // Only visible after at least one of the following folders has been created in Setup and is visible to the current role:
    // Instructor: Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if added to instructor group:  Instructor Files, Course Files, Drop Boxes, Course Repo
    // Designer/TA if not added to instructor group: Course Repo
    // Students: Course Repo, My Drop Box, Groups
    // Observers: Do not display to observers
    public static boolean displayShareAndCollaborate(boolean isInstructor, boolean isTa, boolean isDesigner, boolean isStudent, CourseInit courseInit, DropboxInit dropboxInit) {
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
                if (dropboxInit != null || courseInit.getFileRepoId() != null || courseInit.getGroupsFolderId() != null) {
                    return true;
                }
            }
        }
        // if we made it here, return false
        return false;
    }

    // Only visible after at least one of the following folders has been created in Setup and is visible to the current role:
    // Instructor: Instructor Files, Course Files, Drop Boxes, Course Repo, Groups
    // Designer/TA if added to instructor group:  Instructor Files, Course Files, Drop Boxes, Course Repo, Groups
    // Designer/TA if not added to instructor group: Course Files, Course Repo, Groups
    // Students: Course Files, Course Repo, My Drop Box, Groups
    // Observers: Course Files, Course Repo, Groups
    public static boolean displayFolderWrapper(boolean isInstructor, boolean isTa, boolean isDesigner, boolean isStudent, boolean isObserver, CourseInit courseInit, DropboxInit dropboxInit) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else {
            if (isInstructor && (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                    courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null || courseInit.getGroupsFolderId() != null)) {
                return true;
            } else if (isTa || isDesigner) {
                if ((isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
                    if (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                            courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null || courseInit.getGroupsFolderId() != null) {
                        return true;
                    }
                } else {
                    if (courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null || courseInit.getGroupsFolderId() != null) {
                        return true;
                    }
                }
            } else if (isStudent) {
                if (dropboxInit != null || courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null || courseInit.getGroupsFolderId() != null) {
                    return true;
                }
            } else if (isObserver) {
                if (courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null || courseInit.getGroupsFolderId() != null) {
                    return true;
                }
            }
        }

        // if we made it here, return false
        return false;
    }

    public static boolean displayCourseFilesFolder(String coursefilesFolderId) {
        return coursefilesFolderId != null;
    }

    // Only visible if Drop Boxes have been created in Setup.
    // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
    public static boolean displayDropBoxFolder(boolean isInstructor, boolean isTa, boolean isDesigner, CourseInit courseInit) {
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

    public static boolean displayMyDropBoxFolder(boolean isStudent, DropboxInit dropboxInit) {
        return isStudent && dropboxInit != null;
    }

    public static boolean displayFileRepository(String fileRepoId) {
        return fileRepoId != null;
    }

    // Only visible if Instructor Files folder has been created in Setup.
    // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
    public static boolean displayInstructorFilesFolder(boolean isInstructor, boolean isTa, boolean isDesigner, CourseInit courseInit) {
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

    public static boolean displayGroupsFolder(String groupsFolderId) {
        return groupsFolderId != null;
    }

    // Only visible after a folder or mailing list has been created in Setup.
    public static boolean displayCourseInformation(CourseInit courseInit) {
        if (courseInit == null) {
            // no init, return false
            return false;
        } else if (courseInit.getInstructorFolderId() != null || courseInit.getCoursefilesFolderId() != null ||
                courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null || courseInit.getGroupsFolderId() != null ||
                courseInit.getMailingListAddress() != null) {
            return true;
        }
        return false;
    }
}
