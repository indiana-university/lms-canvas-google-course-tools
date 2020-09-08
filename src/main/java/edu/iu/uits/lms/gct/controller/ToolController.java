package edu.iu.uits.lms.gct.controller;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.drive.model.File;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.amqp.DropboxMessage;
import edu.iu.uits.lms.gct.amqp.DropboxMessageSender;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.model.MainMenuPermissions;
import edu.iu.uits.lms.gct.model.NotificationData;
import edu.iu.uits.lms.gct.model.TokenInfo;
import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import edu.iu.uits.lms.gct.services.MainMenuPermissionsUtil;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.LtiAuthenticationTokenAwareController;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app")
@Slf4j
public class ToolController extends LtiAuthenticationTokenAwareController {

   @Autowired
   private ToolConfig toolConfig = null;

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @Autowired
   private DropboxMessageSender dropboxMessageSender;

   @RequestMapping("/index/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      log.debug("in /index");
      LtiAuthenticationToken token = getValidatedToken(courseId);

      boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
      boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
      boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
      boolean isStudent = request.isUserInRole(LTIConstants.STUDENT_AUTHORITY);
      boolean isObserver = request.isUserInRole(LTIConstants.OBSERVER_AUTHORITY);

      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      String loginId = (String)token.getPrincipal();

      model.addAttribute("courseId", courseId);
      HttpSession session = request.getSession();

      //This should always take precedence
      boolean displayUserIneligibleWarning = !googleCourseToolsService.verifyUserEligibility((String)session.getAttribute(Constants.USER_EMAIL_KEY),
            loginId, (String)session.getAttribute(Constants.USER_SIS_ID_KEY));

      MainMenuPermissions.MainMenuPermissionsBuilder mainMenuPermissionsBuilder = MainMenuPermissions.builder()
            .displayUserIneligibleWarning(displayUserIneligibleWarning);
      String courseTitle = (String)session.getAttribute(Constants.COURSE_TITLE_KEY);

      if (isInstructor && courseInit == null && !displayUserIneligibleWarning) {
            return setup(courseId, model);
      } else if (!displayUserIneligibleWarning && courseInit != null) {

         UserInit ui = googleCourseToolsService.userInitialization(courseId, loginId, courseInit, isInstructor, isTa, isDesigner);

         DropboxInit dropboxInit = googleCourseToolsService.getDropboxInit(courseId, loginId);

         boolean displaySetup = MainMenuPermissionsUtil.displaySetup(isInstructor);
         boolean displaySyncCourseRoster = MainMenuPermissionsUtil.displaySyncCourseRoster(isInstructor);
         boolean displayDiscussInGoogleGroups = MainMenuPermissionsUtil.displayDiscussInGoogleGroups(courseInit.getMailingListAddress());
         boolean displayShareAndCollaborate = MainMenuPermissionsUtil.displayShareAndCollaborate(isInstructor, isTa, isDesigner, isStudent, courseInit, dropboxInit);
         boolean displayFolderWrapper = MainMenuPermissionsUtil.displayFolderWrapper(isInstructor, isTa, isDesigner, isStudent, isObserver, courseInit, dropboxInit);
         boolean displayCourseFilesFolder = MainMenuPermissionsUtil.displayCourseFilesFolder(courseInit.getCoursefilesFolderId());
         boolean displayDropBoxFolder = MainMenuPermissionsUtil.displayDropBoxFolder(isInstructor, isTa, isDesigner, courseInit);
         boolean displayMyDropBoxFolder = MainMenuPermissionsUtil.displayMyDropBoxFolder(isStudent, dropboxInit);
         boolean displayFileRepository = MainMenuPermissionsUtil.displayFileRepository(courseInit.getFileRepoId());
         boolean displayInstructorFilesFolder = MainMenuPermissionsUtil.displayInstructorFilesFolder(isInstructor, isTa, isDesigner, courseInit);
         boolean displayCourseInformation = MainMenuPermissionsUtil.displayCourseInformation(courseInit);

         mainMenuPermissionsBuilder
               .displaySetup(displaySetup)
               .displaySyncCourseRoster(displaySyncCourseRoster)
               .displayDiscussInGoogleGroups(displayDiscussInGoogleGroups)
               .displayShareAndCollaborate(displayShareAndCollaborate)
               .displayFolderWrapper(displayFolderWrapper)
               .displayCourseFilesFolder(displayCourseFilesFolder)
               .displayDropBoxFolder(displayDropBoxFolder)
               .displayMyDropBoxFolder(displayMyDropBoxFolder)
               .displayFileRepository(displayFileRepository)
               .displayInstructorFilesFolder(displayInstructorFilesFolder)
               .displayCourseInformation(displayCourseInformation);
      }
      model.addAttribute("mainMenuPermissions", mainMenuPermissionsBuilder.build());

      return new ModelAndView("index");
   }
//
//   @PostMapping("/createGroup/{courseId}")
//   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
//   public ModelAndView createCourseGroups(@PathVariable("courseId") String courseId, @RequestParam("allMembers") String allEmails,
//                                          @RequestParam("teacherMembers") String teacherEmails, Model model, HttpServletRequest request) {
//      LtiAuthenticationToken token = getValidatedToken(courseId);
//      String courseTitle = (String)token.getData().get(Constants.COURSE_TITLE_KEY);
//      try {
//         Map<Constants.GROUP_TYPES, Group> groups = googleCourseToolsService.createCourseGroups(courseId, courseTitle, false);
//         log.info("Group details: {}", groups);
//
//         List<Member> allMembers = googleCourseToolsService.addMembersToGroup(groups.get(Constants.GROUP_TYPES.ALL).getEmail(), allEmails.split(","));
//         log.info("All Membership details: {}", allMembers);
//
//         List<Member> teacherMembers = googleCourseToolsService.addMembersToGroup(groups.get(Constants.GROUP_TYPES.TEACHER).getEmail(), teacherEmails.split(","));
//         log.info("Teacher Membership details: {}", teacherMembers);
//
//      } catch (IOException e) {
//         log.error("uh oh", e);
//      }
//
//      return index(courseId, model, request);
//   }
//
//   @PostMapping("/groups/{courseId}")
//   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
//   public ModelAndView getGroups(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
//      try {
//         List<Group> groups = googleCourseToolsService.getGroupsForCourse(courseId);
//
//         model.addAttribute("groups", groups);
////         log.info("Group details: {}", groups);
//      } catch (IOException e) {
//         log.error("uh oh", e);
//      }
//
//      return index(courseId, model, request);
//   }
//
//   @PostMapping("/files/{courseId}")
//   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
//   public ModelAndView getFiles(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
//      try {
//         List<File> files = googleCourseToolsService.getDriveFiles();
//         model.addAttribute("files", files);
////         log.info("File details: {}", files);
//      } catch (IOException e) {
//         log.error("uh oh", e);
//      }
//
//      return index(courseId, model, request);
//   }
//
//   @PostMapping("/folders/{courseId}")
//   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
//   public ModelAndView initFolders(@PathVariable("courseId") String courseId,
//                                   @RequestParam("launchedUser") String launchedUser, Model model, HttpServletRequest request) {
//      try {
//         List<String> folderIds = googleCourseToolsService.initBaseFolders();
//         File userFolder = googleCourseToolsService.createUserRootFolder(launchedUser + "@iu.edu", launchedUser);
////         model.addAttribute("files", files);
////         log.info("File details: {}", files);
//      } catch (IOException e) {
//         log.error("uh oh", e);
//      }
//
//      return index(courseId, model, request);
//   }

   @PostMapping("/picker/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView picker(@PathVariable("courseId") String courseId, Model model) {
      model.addAttribute("courseId", courseId);

      TokenInfo pickerTokenInfo = googleCourseToolsService.getPickerTokenInfo();

      model.addAttribute("pickerTokenInfo", pickerTokenInfo);

      return new ModelAndView("picker");
   }

   @RequestMapping("/setup/{courseId}")
   @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
   public ModelAndView setup(@PathVariable("courseId") String courseId, Model model) {
      model.addAttribute("courseId", courseId);
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      model.addAttribute("courseInit", courseInit);

      return new ModelAndView("setup");
   }

   @PostMapping("/setupSubmit/{courseId}")
   @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
   public ModelAndView setupSubmit(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request,
                                   @RequestParam(value="createCourseFileFolder", required = false) boolean createCourseFileFolder,
                                   @RequestParam(value="createInstructorFileFolder", required = false) boolean createInstructorFileFolder,
                                   @RequestParam(value="createDropboxFolder", required = false) boolean createDropboxFolder,
                                   @RequestParam(value="createFileRepositoryFolder", required = false) boolean createFileRepositoryFolder,
                                   @RequestParam(value="createMailingList", required = false) boolean createMailingList,
                                   @RequestParam(value="taAccess", required = false) boolean taAccess,
                                   @RequestParam(value="designerAccess", required = false) boolean designerAccess) {

      LtiAuthenticationToken token = getValidatedToken(courseId);
      boolean updatedSomething = false;
      boolean sendNotification = false;
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      String courseTitle = (String)request.getSession().getAttribute(Constants.COURSE_TITLE_KEY);
      String loginId = (String)token.getPrincipal();

      if (courseInit == null) {
         courseInit = googleCourseToolsService.courseInitialization(courseId, courseTitle, loginId);
         //Only want to send the notification the first time through
         sendNotification = true;
      }
      List<String> errors = new ArrayList<>();
      NotificationData notificationData = new NotificationData();
      notificationData.setCourseTitle(courseTitle);

      String allGroupEmail = "";
      String allGroupName = "";
      String teacherGroupEmail = "";

      // get some official group emails here to not call this repeatedly in multiple methods in googleCourseToolsService
      try {
         Map<Constants.GROUP_TYPES, Group> groups = googleCourseToolsService.getGroupsForCourse(courseId);
         allGroupEmail = groups.get(Constants.GROUP_TYPES.ALL).getEmail();
         allGroupName = groups.get(Constants.GROUP_TYPES.ALL).getName();
         teacherGroupEmail = groups.get(Constants.GROUP_TYPES.TEACHER).getEmail();
         notificationData.setAllGroup(groups.get(Constants.GROUP_TYPES.ALL));
         notificationData.setTeacherGroup(groups.get(Constants.GROUP_TYPES.TEACHER));

         File courseFolder = googleCourseToolsService.getFolder(courseInit.getCourseFolderId());
         notificationData.setRootCourseFolder(courseFolder.getName());
      } catch (IOException e) {
         // something bad happened, so let's bail on it all
         errors.add("Error getting group info from Google. Bailing on setup changes.");
         model.addAttribute("setupErrors", errors);
         return index(courseId, model, request);
      }

      if (createCourseFileFolder) {
         try {
            File courseFilesFolder = googleCourseToolsService.createCourseFileFolder(courseId, courseTitle, teacherGroupEmail);
            courseInit.setCoursefilesFolderId(courseFilesFolder.getId());
            notificationData.setCourseFilesFolder(courseFilesFolder.getName());
         } catch (IOException e) {
            String courseFilesFolderError = "Issue with creating the course file folder";
            errors.add(courseFilesFolderError);
            log.error(courseFilesFolderError, e);
         }
         updatedSomething = true;
      }

      if (createInstructorFileFolder) {
         try {
            File instructorFilesFolder = googleCourseToolsService.createInstructorFileFolder(courseId, courseTitle, allGroupEmail, teacherGroupEmail);
            courseInit.setInstructorFolderId(instructorFilesFolder.getId());
            notificationData.setInstructorFilesFolder(instructorFilesFolder.getName());
         } catch (IOException e) {
            String instructorFolderError = "Issue with creating the instructor file folder";
            errors.add(instructorFolderError);
            log.error(instructorFolderError, e);
         }
         updatedSomething = true;
      }

      if (createDropboxFolder) {
         try {
            File dropboxFolder = googleCourseToolsService.createDropboxFolder(courseId, courseTitle);
            String dropboxFolderId = dropboxFolder.getId();
            courseInit.setDropboxFolderId(dropboxFolderId);
            notificationData.setDropboxFilesFolder(dropboxFolder.getName());

            //Create student dropboxes by pushing a message to the queue
            DropboxMessage dm = DropboxMessage.builder().courseId(courseId).courseTitle(courseTitle).dropboxFolderId(dropboxFolderId)
                  .allGroupEmail(allGroupEmail).teacherGroupEmail(teacherGroupEmail).build();
            dropboxMessageSender.send(dm);
         } catch (IOException e) {
            String dropboxFolderError = "Issue with creating the dropbox file folder";
            errors.add(dropboxFolderError);
            log.error(dropboxFolderError, e);
         }
         updatedSomething = true;
      }

      if (createFileRepositoryFolder) {
         try {
            File fileRepositoryFolder = googleCourseToolsService.createFileRepositoryFolder(courseId, courseTitle, allGroupEmail);
            courseInit.setFileRepoId(fileRepositoryFolder.getId());
            notificationData.setFileRepositoryFolder(fileRepositoryFolder.getName());
         } catch (IOException e) {
            String fileRepoError = "Issue with creating the file repository folder";
            errors.add(fileRepoError);
            log.error(fileRepoError, e);
         }
         updatedSomething = true;
      }

      // TODO - do whatever we need to do to actually create the mx record
      if (createMailingList) {
         courseInit.setMailingListAddress(allGroupEmail);
         notificationData.setMailingListAddress(allGroupEmail);
         notificationData.setMailingListName(allGroupName);
         updatedSomething = true;
      }

      // any changes to TA or Designer access?
      if (courseInit.isTaTeacher() != taAccess || courseInit.isDeTeacher() != designerAccess) {
         courseInit.setTaTeacher(taAccess);
         courseInit.setDeTeacher(designerAccess);
         updatedSomething = true;
      }

      // if something got updated, then call the save to courseInit
      if (updatedSomething) {
         try {
            googleCourseToolsService.saveCourseInit(courseInit);
         } catch (Exception e) {
            String saveCourseInitError = "There was an error saving the data";
            errors.add(saveCourseInitError);
            log.error(saveCourseInitError, e);
         }
         if (sendNotification) {
            googleCourseToolsService.sendCourseSetupNotification(courseInit, notificationData);
         }
      }

      if (errors.isEmpty()) {
         // if a user hit submit and there were no changes, don't bother with a success message
         if (updatedSomething) {
            model.addAttribute("setupSuccess", "The changes submitted in the setup page were successful!");
         }
      } else {
         model.addAttribute("setupErrors", errors);
      }

      return index(courseId, model, request);
   }

   @RequestMapping(value="/setupSubmit/{courseId}", params="action=setupCancel")
   @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
   public ModelAndView setupCancel(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      return index(courseId, model, request);
   }
}
