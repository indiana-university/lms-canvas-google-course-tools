package edu.iu.uits.lms.gct.controller;

import canvas.client.generated.model.CourseGroup;
import com.google.api.services.drive.model.File;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.Constants.FOLDER_TYPES;
import edu.iu.uits.lms.gct.amqp.DropboxMessage;
import edu.iu.uits.lms.gct.amqp.DropboxMessageSender;
import edu.iu.uits.lms.gct.amqp.RosterSyncMessage;
import edu.iu.uits.lms.gct.amqp.RosterSyncMessageSender;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseGroupWrapper;
import edu.iu.uits.lms.gct.model.CourseInfo;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.model.MainMenuPermissions;
import edu.iu.uits.lms.gct.model.MenuFolderLink;
import edu.iu.uits.lms.gct.model.NotificationData;
import edu.iu.uits.lms.gct.model.RosterSyncCourseData;
import edu.iu.uits.lms.gct.model.SerializableGroup;
import edu.iu.uits.lms.gct.model.SharedFilePermission;
import edu.iu.uits.lms.gct.model.SharedFilePermissionModel;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

   @Autowired
   private RosterSyncMessageSender rosterSyncMessageSender;

   @Autowired
   private CourseSessionService courseSessionService;

   @RequestMapping("/loading/{courseId}")
   public String loading(@PathVariable("courseId") String courseId, Model model) {
      model.addAttribute("courseId", courseId);
      model.addAttribute("hideFooter", true);
      return "loading";
   }

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
      String userEmail = courseSessionService.getAttributeFromSession(session, courseId, Constants.USER_EMAIL_KEY, String.class);
      String userSisId = courseSessionService.getAttributeFromSession(session, courseId, Constants.USER_SIS_ID_KEY, String.class);
      boolean displayUserIneligibleWarning = !googleCourseToolsService.verifyUserEligibility(userEmail, loginId, userSisId);

      MainMenuPermissions.MainMenuPermissionsBuilder mainMenuPermissionsBuilder = MainMenuPermissions.builder()
            .displayUserIneligibleWarning(displayUserIneligibleWarning);
      String courseTitle = courseSessionService.getAttributeFromSession(session, courseId, Constants.COURSE_TITLE_KEY, String.class);

      if (isInstructor && courseInit == null && !displayUserIneligibleWarning) {
         if (googleCourseToolsService.titleHasInvalidCharacters(courseTitle)) {
            mainMenuPermissionsBuilder.displayBadCourseTitleWarning(true);
         } else {
            return setup(courseId, model);
         }
      } else if (!displayUserIneligibleWarning && courseInit != null) {

         DropboxInit dropboxInit = googleCourseToolsService.getDropboxInit(courseId, loginId);

         try {
            // Make sure that groups exist.
            // There could be a weird case (not likely in prd though) where the course was initialized
            // in one env (dev) but when a user is being initialized in another env (reg) the groups are missing.
            CourseGroupWrapper groupsForCourse = getGroupsForCourse(courseId, request);
            if (groupsForCourse == null || !groupsForCourse.hasRequiredGroups()) {
               googleCourseToolsService.createCourseGroups(courseId, courseTitle, courseInit.getMailingListAddress() != null);
            }
            UserInit ui = googleCourseToolsService.userInitialization(courseId, loginId, courseInit, isInstructor, isTa, isDesigner);
            model.addAttribute("googleLoginId", ui.getGoogleLoginId());

            //Check to see if the student should have a dropbox but doesn't
            if (isStudent && courseInit.getDropboxFolderId() != null && dropboxInit == null) {
               dropboxInit = googleCourseToolsService.createStudentDropboxFolder(courseId, courseTitle, courseInit.getDropboxFolderId(),
                     loginId, groupsForCourse.getAllGroup().getEmail(), groupsForCourse.getTeacherGroup().getEmail(),
                     dropboxInit);
            }

         } catch (IOException e) {
            log.error("Can't get course groups");
         }

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
         boolean displayGroupsFolder = MainMenuPermissionsUtil.displayGroupsFolder(courseInit.getGroupsFolderId());
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
               .displayGroupsFolder(displayGroupsFolder)
               .displayCourseInformation(displayCourseInformation);

         List<MenuFolderLink> menuFolderLinks = new ArrayList<>();

         if (displayCourseFilesFolder) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(courseInit.getCoursefilesFolderId()),
                  FOLDER_TYPES.courseFiles.getText()));
         }
         if (displayDropBoxFolder) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(courseInit.getDropboxFolderId()),
                  FOLDER_TYPES.dropBoxes.getText()));
         }
         if (displayMyDropBoxFolder) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(dropboxInit.getFolderId()),
                  FOLDER_TYPES.mydropBox.getText()));
         }
         if (displayFileRepository) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(courseInit.getFileRepoId()),
                  FOLDER_TYPES.fileRepository.getText()));
         }
         if (displayInstructorFilesFolder) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(courseInit.getInstructorFolderId()),
                  FOLDER_TYPES.instructorFiles.getText()));
         }
         if (displayGroupsFolder) {
            menuFolderLinks.add(new MenuFolderLink(getFolderLink(courseInit.getGroupsFolderId()),
                  FOLDER_TYPES.groupsFiles.getText()));
         }
         model.addAttribute("menuFolderLinks", menuFolderLinks);
      }
      if (displayUserIneligibleWarning) {
         StringBuilder text = new StringBuilder("We're sorry. This tool cannot be used by IU guests. However, the instructor can add you to the Google groups for the course manually, which will allow you to access course resources directly in Google. To request to be added, please contact your instructor and include this link, which provides instructions, in your message: https://kb.iu.edu/d/bgjk#grant-remove.");
         if (courseInit != null && courseInit.getCourseFolderId() != null) {
            try {
               File folder = googleCourseToolsService.getFolder(courseInit.getCourseFolderId());
               String messagePattern = "<div class=\"rvt-m-top-xs\">Once you have been added, use the link below to navigate to the top-level folder for the course:</div>" +
                     "<div class=\"rvt-m-top-xs\"><a href=\"{0}\" target=\"_blank\">{1}</a></div>" +
                     "<div class=\"rvt-m-top-xs\">Additional information is available at <a href=\"https://kb.iu.edu/d/bgjk\" target=\"_blank\">https://kb.iu.edu/d/bgjk</a>.</div>";
               String warningMessageExtras = MessageFormat.format(messagePattern, folder.getWebViewLink(), folder.getName());
               text.append(warningMessageExtras);
            } catch (IOException e) {
               log.warn("Unable to get folder", e);
            }
         }
         mainMenuPermissionsBuilder.userIneligibleWarningText(text.toString());
      }

      model.addAttribute("mainMenuPermissions", mainMenuPermissionsBuilder.build());

      return new ModelAndView("index");
   }

   /**
    * Get the webview link for a given folder, wrapping it in a google auth url, or return null
    * @param folderId
    * @return
    */
   private String getFolderLink(String folderId) {
      String link = null;
      try {
         File folder = googleCourseToolsService.getFolder(folderId);
         link = googleCourseToolsService.authWrapUrl(folder.getWebViewLink());
      } catch (IOException e) {
         log.error("Unable to get folder", e);
      }
      return link;
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
                                   @RequestParam(value="createGroupsFolder", required = false) boolean createCanvasGroupsFolder,
                                   @RequestParam(value="createDropboxFolder", required = false) boolean createDropboxFolder,
                                   @RequestParam(value="createFileRepositoryFolder", required = false) boolean createFileRepositoryFolder,
                                   @RequestParam(value="createMailingList", required = false) boolean createMailingList,
                                   @RequestParam(value="taAccess", required = false) boolean taAccess,
                                   @RequestParam(value="designerAccess", required = false) boolean designerAccess) {

      LtiAuthenticationToken token = getValidatedToken(courseId);
      boolean updatedSomething = false;
      boolean sendNotification = false;
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      HttpSession session = request.getSession();
      String courseTitle = courseSessionService.getAttributeFromSession(session, courseId, Constants.COURSE_TITLE_KEY, String.class);
      String courseSisId = courseSessionService.getAttributeFromSession(session, courseId, Constants.COURSE_SIS_ID_KEY, String.class);
      String courseCode = courseSessionService.getAttributeFromSession(session, courseId, Constants.COURSE_CODE_KEY, String.class);

      List<String> errors = new ArrayList<>();
      if (courseInit == null) {

         try {
            courseInit = googleCourseToolsService.courseInitialization(courseId, courseTitle, courseSisId, courseCode, createMailingList);
            //Only want to send the notification the first time through
            sendNotification = true;
         } catch (IOException e) {
            log.error("Error during course initialization", e);
            // Have to go to the setup page instead of index, because we'll just get redirected back to setup anyway and lose the error message
            errors.add("Error during course initialization. Settings were not saved.  Please try again.");
            model.addAttribute("setupErrors", errors);
            return setup(courseId, model);
         }
      }

      NotificationData notificationData = new NotificationData();
      notificationData.setCourseTitle(courseTitle);

      String allGroupEmail = "";
      String allGroupName = "";
      String teacherGroupEmail = "";

      // get some official group emails here to not call this repeatedly in multiple methods in googleCourseToolsService
      try {
         CourseGroupWrapper groups = getGroupsForCourse(courseId, request);
         allGroupEmail = groups.getAllGroup().getEmail();
         allGroupName = groups.getAllGroup().getName();
         teacherGroupEmail = groups.getTeacherGroup().getEmail();
         notificationData.setAllGroup(groups.getAllGroup());
         notificationData.setTeacherGroup(groups.getTeacherGroup());

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

      if (createCanvasGroupsFolder) {
         try {
            File groupsFolder = googleCourseToolsService.createCanvasGroupsFolder(courseId, courseTitle, allGroupEmail, teacherGroupEmail);
            courseInit.setGroupsFolderId(groupsFolder.getId());
            notificationData.setGroupsFolder(groupsFolder.getName());
         } catch (IOException e) {
            String groupsFolderError = "Issue with creating the groups file folder";
            errors.add(groupsFolderError);
            log.error(groupsFolderError, e);
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
      // TODO - Might also need to update some settings for the all group
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

   @RequestMapping(value={"/setupSubmit/{courseId}", "/share/perms/{courseId}", "/share/perms/{courseId}/submit"}, params="action=setupCancel")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView setupCancel(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      log.debug("in /setupCancel");
      return index(courseId, model, request);
   }

   @RequestMapping("/info/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView info(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
      model.addAttribute("courseId", courseId);
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      CourseInfo courseInfo = new CourseInfo();
      List<String> optionalCourseFolders = new ArrayList<>();
      try {
         //Get group stuff
         CourseGroupWrapper groupsForCourse = getGroupsForCourse(courseId, request);
         SerializableGroup allGroup = groupsForCourse.getAllGroup();
         String allGroupUrl = googleCourseToolsService.buildGroupUrlFromEmail(allGroup.getEmail());
         courseInfo.setAllGroupDetails(new CourseInfo.GroupDetails(allGroup, googleCourseToolsService.authWrapUrl(allGroupUrl)));

         SerializableGroup teacherGroup = groupsForCourse.getTeacherGroup();
         String teacherGroupUrl = googleCourseToolsService.buildGroupUrlFromEmail(teacherGroup.getEmail());
         courseInfo.setTeacherGroupDetails(new CourseInfo.GroupDetails(teacherGroup, googleCourseToolsService.authWrapUrl(teacherGroupUrl)));

         //Get Canvas group stuff
         List<CourseGroup> canvasCourseGroups = googleCourseToolsService.getCanvasGroupsForCourse(courseId);
         List<String> canvasGroupEmails = canvasCourseGroups.stream()
               .map(googleCourseToolsService::getEmailForCourseGroup)
               .map(String::toLowerCase)
               .collect(Collectors.toList());

         for (SerializableGroup group : groupsForCourse.getCanvasGroups()) {
            String groupUrl = googleCourseToolsService.buildGroupUrlFromEmail(group.getEmail());
            boolean existsInCanvas = canvasGroupEmails.contains(group.getEmail().toLowerCase());
            courseInfo.addCanvasCourseGroup(new CourseInfo.CanvasGroupDetails(group, googleCourseToolsService.authWrapUrl(groupUrl), existsInCanvas));
         }

         //Sort the canvas course groups
         courseInfo.getCanvasCourseGroups().sort(Comparator.comparing(CourseInfo.GroupDetails::getName));

         //Get course folders
         File courseFolder = googleCourseToolsService.getFolder(courseInit.getCourseFolderId());
         courseInfo.setRootCourseFolder(courseFolder.getName());

         if (courseInit.getCoursefilesFolderId() != null) {
            File folder = googleCourseToolsService.getFolder(courseInit.getCoursefilesFolderId());
            optionalCourseFolders.add(folder.getName());
         }

         if (courseInit.getInstructorFolderId() != null) {
            File folder = googleCourseToolsService.getFolder(courseInit.getInstructorFolderId());
            optionalCourseFolders.add(folder.getName());
         }

         if (courseInit.getGroupsFolderId() != null) {
            File folder = googleCourseToolsService.getFolder(courseInit.getGroupsFolderId());
            optionalCourseFolders.add(folder.getName());
         }

         if (courseInit.getDropboxFolderId() != null) {
            File folder = googleCourseToolsService.getFolder(courseInit.getDropboxFolderId());
            optionalCourseFolders.add(folder.getName());
         }

         if (courseInit.getFileRepoId() != null) {
            File folder = googleCourseToolsService.getFolder(courseInit.getFileRepoId());
            optionalCourseFolders.add(folder.getName());
         }

      } catch (IOException e) {
         log.error("Unable to get information for course");
      }

      List<String> teacherRoles = new ArrayList<>();
      teacherRoles.add("Teachers");
      if (courseInit.isTaTeacher()) {
         teacherRoles.add("TAs");
      }
      if (courseInit.isDeTeacher()) {
         teacherRoles.add("Designers");
      }
      courseInfo.setTeacherRoles(teacherRoles);
      courseInfo.setInstructor(isInstructor);
      courseInfo.setMailingListEnabled(courseInit.getMailingListAddress() != null);

      courseInfo.setOptionalCourseFolders(optionalCourseFolders);

      model.addAttribute("courseInfo", courseInfo);

      return new ModelAndView("info");
   }

   @RequestMapping("/share/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView share(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      log.debug("in /share");
      TokenInfo pickerTokenInfo = googleCourseToolsService.getPickerTokenInfo();
      model.addAttribute("pickerTokenInfo", pickerTokenInfo);

      boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
      boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
      boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
      boolean isStudent = request.isUserInRole(LTIConstants.STUDENT_AUTHORITY);
//      boolean isObserver = request.isUserInRole(LTIConstants.OBSERVER_AUTHORITY);
      model.addAttribute("courseId", courseId);
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);

      List<FOLDER_TYPES> availableFolders = new ArrayList<>();
      boolean isTaTeacher = isTa && courseInit.isTaTeacher();
      boolean isDeTeacher = isDesigner && courseInit.isDeTeacher();

      if (courseInit.getCoursefilesFolderId() != null && (isInstructor || isTaTeacher || isDeTeacher)) {
         availableFolders.add(FOLDER_TYPES.courseFiles);
      }

      if (courseInit.getInstructorFolderId() != null && (isInstructor || isTaTeacher || isDeTeacher)) {
         availableFolders.add(FOLDER_TYPES.instructorFiles);
      }

      if (courseInit.getDropboxFolderId() != null && isStudent) {
         availableFolders.add(FOLDER_TYPES.mydropBox);
      }

      if (courseInit.getFileRepoId() != null) {
         availableFolders.add(FOLDER_TYPES.fileRepository);
      }

      model.addAttribute("availableFolders", availableFolders);
      return new ModelAndView("share");
   }

   @RequestMapping("/share/perms/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView perms(@PathVariable("courseId") String courseId,
                             @RequestParam("fileIds[]") String[] fileIds,
                             @RequestParam("destFolder") String destFolder, Model model, HttpServletRequest request) {
      log.debug("in /share/perms");
//      log.debug("{}", fileIds);
      LtiAuthenticationToken token = getValidatedToken(courseId);
      boolean showAll = false;

      try {
         String loginId = (String)token.getPrincipal();
         List<File> allFiles = googleCourseToolsService.getFiles(fileIds, loginId);

         CourseGroupWrapper groupsForCourse = getGroupsForCourse(courseId, request);

         final String defaultPerm = FOLDER_TYPES.mydropBox.name().equals(destFolder) ?
               Constants.PERMISSION_ROLES.commenter.name() :
               Constants.PERMISSION_ROLES.reader.name();

         List<SharedFilePermission> sharedFilePermissions = allFiles.stream()
               .map(file -> new SharedFilePermission(file,
                     GoogleCourseToolsService.getExistingRoleForGroupPerm(file.getPermissions(), groupsForCourse.getAllGroup().getEmail(), defaultPerm),
                     GoogleCourseToolsService.getExistingRoleForGroupPerm(file.getPermissions(), groupsForCourse.getTeacherGroup().getEmail(), defaultPerm)))
               .sorted(Comparator.comparing(SharedFilePermission::isFolder).reversed()
                     .thenComparing(sharedFilePermission -> sharedFilePermission.getFile().getName()))
               .collect(Collectors.toList());

         model.addAttribute("sharedFilePermissionModel", new SharedFilePermissionModel(sharedFilePermissions, FOLDER_TYPES.valueOf(destFolder)));

      } catch (IOException e) {
         log.error("unable to parse json into object", e);
      }

      if (FOLDER_TYPES.courseFiles.name().equals(destFolder) || FOLDER_TYPES.fileRepository.name().equals(destFolder)) {
         showAll = true;
      }
      model.addAttribute("showAll", showAll);

      return new ModelAndView("share_perms");
   }

   @PostMapping("/share/perms/{courseId}/submit")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView permsSubmit(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request,
                                   @ModelAttribute SharedFilePermissionModel sharedFilePermissionModel) {
      LtiAuthenticationToken token = getValidatedToken(courseId);
      String loginId = (String)token.getPrincipal();
      log.debug("{}", sharedFilePermissionModel);
      List<String> errors = new ArrayList<>();

      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      DropboxInit dropboxInit = googleCourseToolsService.getDropboxInit(courseId, loginId);

      String destFolderId = getSelectedFolderId(sharedFilePermissionModel.getDestFolderType(), courseInit, dropboxInit);

      try {
         CourseGroupWrapper groupsForCourse = getGroupsForCourse(courseId, request);

         for (SharedFilePermission sharedFilePermission : sharedFilePermissionModel.getSharedFilePermissions()) {
            try {
               googleCourseToolsService.shareAndAddShortcut(sharedFilePermission.getFile().getId(), destFolderId,
                     groupsForCourse, sharedFilePermission.getAllPerm(), sharedFilePermission.getTeacherPerm(), loginId);
            } catch (IOException e) {
               log.error("error with setting permissions", e);
               errors.add("There were problems when sharing " + sharedFilePermission.getFile().getName());
            }
         }
      } catch (IOException e) {
         log.error("error getting course groups", e);
         errors.add("Error getting group info from Google. Bailing on permissions changes.");
      }

      if (errors.isEmpty()) {
         model.addAttribute("setupSuccess", "The changes submitted in the permissions page were successful!");
      } else {
         model.addAttribute("setupErrors", errors);
      }

      return index(courseId, model, request);
   }

   @RequestMapping("/sync/{courseId}")
   @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
   public ModelAndView rosterSync(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
      try {
         CourseGroupWrapper groups = getGroupsForCourse(courseId, request);
         String allGroupEmail = groups.getAllGroup().getEmail();
         String teacherGroupEmail = groups.getTeacherGroup().getEmail();
         String courseTitle = courseSessionService.getAttributeFromSession(request.getSession(), courseId, Constants.COURSE_TITLE_KEY, String.class);
         RosterSyncMessage rsm = RosterSyncMessage.builder()
               .courseData(new RosterSyncCourseData(courseId, courseTitle, allGroupEmail, teacherGroupEmail))
               .sendNotificationForCourse(true)
               .build();
         rosterSyncMessageSender.send(rsm);

         model.addAttribute("setupSuccess", "The roster syncing process may take a few minutes.  You will receive a Canvas notification when it is complete.");

      } catch (IOException e) {
         log.error("unable to get course groups", e);
         List<String> errors = Collections.singletonList("Unable to get course groups - roster sync has failed");
         model.addAttribute("setupErrors", errors);
      }
      return index(courseId, model, request);
   }

   private CourseGroupWrapper getGroupsForCourse(String courseId, HttpServletRequest request) throws IOException {
      HttpSession session = request.getSession();

      CourseGroupWrapper courseGroups = courseSessionService.getAttributeFromSession(session, courseId, Constants.COURSE_GROUPS_KEY, CourseGroupWrapper.class);

      if (courseGroups == null) {
         courseGroups = googleCourseToolsService.getGroupsForCourse(courseId);
         courseSessionService.addAttributeToSession(session, courseId, Constants.COURSE_GROUPS_KEY, courseGroups);
      }

      return courseGroups;
   }

   private String getSelectedFolderId(FOLDER_TYPES folderType, CourseInit courseInit, DropboxInit dropboxInit) {
      String folderId = null;
      switch (folderType) {
         case courseFiles:
            folderId = courseInit.getCoursefilesFolderId();
            break;
         case instructorFiles:
            folderId = courseInit.getInstructorFolderId();
            break;
         case dropBoxes:
            folderId = courseInit.getDropboxFolderId();
            break;
         case fileRepository:
            folderId = courseInit.getFileRepoId();
            break;
         case mydropBox:
            folderId = dropboxInit.getFolderId();
      }
      return folderId;
   }
}