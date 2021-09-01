package edu.iu.uits.lms.gct.services;

import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class MainMenuPermissionsUtilTest {

    @Test
    public void displaySetupTest() throws Exception {
        boolean isInstructor = true;
        Assert.assertTrue(MainMenuPermissionsUtil.displaySetup(isInstructor));

        isInstructor = false;
        Assert.assertFalse(MainMenuPermissionsUtil.displaySetup(isInstructor));
    }

    @Test
    public void displaySyncCourseRosterTest() throws Exception {
        boolean isInstructor = true;
        Assert.assertTrue(MainMenuPermissionsUtil.displaySyncCourseRoster(isInstructor));

        isInstructor = false;
        Assert.assertFalse(MainMenuPermissionsUtil.displaySyncCourseRoster(isInstructor));
    }

    @Test
    public void displayDiscussInGoogleGroupsTest() throws Exception {
        String mailingListAddress = null;
        Assert.assertFalse(MainMenuPermissionsUtil.displayDiscussInGoogleGroups(mailingListAddress));

        mailingListAddress = "1234";
        Assert.assertTrue(MainMenuPermissionsUtil.displayDiscussInGoogleGroups(mailingListAddress));
    }

    @Test
    public void displayShareAndCollaborateTest() throws Exception {
        boolean isInstructor = false;
        boolean isTa = false;
        boolean isDesigner = false;
        boolean isStudent = false;
        CourseInit courseInit = null;
        DropboxInit dropboxInit = null;

        // test a null courseInit
        Assert.assertFalse(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit = new CourseInit();
        courseInit.setCourseId("1234");

        // instructor section
        isInstructor = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        isInstructor = false;
        courseInit.setFileRepoId(null);

        // TA section
        isTa = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setTaTeacher(true);
        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        isTa = false;
        courseInit.setFileRepoId(null);
        courseInit.setTaTeacher(false);

        // designer section
        isDesigner = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setDeTeacher(true);
        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        isDesigner = false;
        courseInit.setFileRepoId(null);
        courseInit.setDeTeacher(false);

        // student section
        isStudent = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        dropboxInit = new DropboxInit();
        dropboxInit.setCourseId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setDropboxFolderId(null);
        courseInit.setGroupsFolderId("1234");
        dropboxInit = null;
        Assert.assertTrue(MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit));
    }

    @Test
    public void displayFolderWrapperTest() throws Exception {
        boolean isInstructor = false;
        boolean isTa = false;
        boolean isDesigner = false;
        boolean isStudent = false;
        boolean isObserver = false;
        CourseInit courseInit = null;
        DropboxInit dropboxInit = null;

        // test a null courseInit
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit = new CourseInit();
        courseInit.setCourseId("1234");

        // instructor section
        isInstructor = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        isInstructor = false;
        courseInit.setFileRepoId(null);

        // TA section
        isTa = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setTaTeacher(true);
        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        isTa = false;
        courseInit.setFileRepoId(null);
        courseInit.setTaTeacher(false);

        // designer section
        isDesigner = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDeTeacher(true);
        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        isDesigner = false;
        courseInit.setFileRepoId(null);
        courseInit.setDeTeacher(false);

        // student section
        isStudent = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setCoursefilesFolderId(null);
        dropboxInit = new DropboxInit();
        dropboxInit.setCourseId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        isStudent = false;
        dropboxInit = null;

        // observer section
        isObserver = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));

        courseInit.setFileRepoId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit));
    }

    @Test
    public void displayCourseFilesFolderTest() throws Exception {
        String coursefilesFolderId = null;
        Assert.assertFalse(MainMenuPermissionsUtil.displayCourseFilesFolder(coursefilesFolderId));

        coursefilesFolderId = "1234";
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseFilesFolder(coursefilesFolderId));
    }

    @Test
    public void displayDropBoxFolderTest() throws Exception {
        boolean isInstructor = false;
        boolean isTa = false;
        boolean isDesigner = false;
        CourseInit courseInit = null;

        // test a null courseInit
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit = new CourseInit();
        courseInit.setCourseId("1234");

        // instructor section
        isInstructor = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        isInstructor = false;
        courseInit.setDropboxFolderId(null);

        // TA section
        isTa = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDropboxFolderId("1234");
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setTaTeacher(true);
        Assert.assertTrue(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        isTa = false;
        courseInit.setDropboxFolderId(null);

        // designer section
        isDesigner = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDropboxFolderId("1234");
        Assert.assertFalse(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDeTeacher(true);
        Assert.assertTrue(MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit));
    }

    @Test
    public void displayMyDropBoxFolderTest() throws Exception {
        boolean isStudent = false;
        DropboxInit dropboxInit = null;

        Assert.assertFalse(MainMenuPermissionsUtil.displayMyDropBoxFolder(isStudent, dropboxInit));

        isStudent = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayMyDropBoxFolder(isStudent, dropboxInit));

        isStudent = false;
        dropboxInit = new DropboxInit();
        dropboxInit.setCourseId("1234");
        Assert.assertFalse(MainMenuPermissionsUtil.displayMyDropBoxFolder(isStudent, dropboxInit));

        isStudent = true;
        Assert.assertTrue(MainMenuPermissionsUtil.displayMyDropBoxFolder(isStudent, dropboxInit));
    }

    @Test
    public void displayFileRepositoryTest() throws Exception {
        String fileRepoId = null;
        Assert.assertFalse(MainMenuPermissionsUtil.displayFileRepository(fileRepoId));

        fileRepoId = "1234";
        Assert.assertTrue(MainMenuPermissionsUtil.displayFileRepository(fileRepoId));
    }

    @Test
    public void displayInstructorFilesFolderTest() throws Exception {
        boolean isInstructor = false;
        boolean isTa = false;
        boolean isDesigner = false;
        CourseInit courseInit = null;

        // test a null courseInit
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit = new CourseInit();
        courseInit.setCourseId("1234");

        // instructor section
        isInstructor = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        isInstructor = false;
        courseInit.setDropboxFolderId(null);

        // TA section
        isTa = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDropboxFolderId("1234");
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setTaTeacher(true);
        Assert.assertTrue(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        isTa = false;
        courseInit.setDropboxFolderId(null);

        // designer section
        isDesigner = true;
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDropboxFolderId("1234");
        Assert.assertFalse(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));

        courseInit.setDeTeacher(true);
        Assert.assertTrue(MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit));
    }

    @Test
    public void displayCourseInformationTest() throws Exception {
        CourseInit courseInit = null;

        // test a null courseInit
        Assert.assertFalse(MainMenuPermissionsUtil.displayCourseInformation(courseInit));

        courseInit = new CourseInit();
        courseInit.setCourseId("1234");

        // instructor folder
        courseInit.setInstructorFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseInformation(courseInit));

        // course files folder
        courseInit.setInstructorFolderId(null);
        courseInit.setCoursefilesFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseInformation(courseInit));

        // dropbox folder
        courseInit.setCoursefilesFolderId(null);
        courseInit.setDropboxFolderId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseInformation(courseInit));

        // course repository folder
        courseInit.setDropboxFolderId(null);
        courseInit.setFileRepoId("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseInformation(courseInit));

        // mailing address
        courseInit.setFileRepoId(null);
        courseInit.setMailingListAddress("1234");
        Assert.assertTrue(MainMenuPermissionsUtil.displayCourseInformation(courseInit));
    }
}
