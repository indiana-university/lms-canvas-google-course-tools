package edu.iu.uits.lms.gct.controller;

import com.google.api.services.admin.directory.model.Group;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.model.MainMenuPermissions;
import edu.iu.uits.lms.gct.model.TokenInfo;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/app")
@Slf4j
public class ToolController extends LtiAuthenticationTokenAwareController {

   @Autowired
   private ToolConfig toolConfig = null;

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

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

      //For session tracking
//      model.addAttribute("customId", httpSession.getId());
      model.addAttribute("courseId", courseId);

      if (isInstructor && courseInit == null) {
         String courseTitle = (String)request.getSession().getAttribute(Constants.COURSE_TITLE_KEY);
         courseInit = googleCourseToolsService.initialize(courseId, courseTitle, loginId);
         if (courseInit != null) {
            return setup(courseId, model);
         } else {
            model.addAttribute("initError", "There were errors in the initialization process.");
            MainMenuPermissions mainMenuPermissions = new MainMenuPermissions();
            model.addAttribute("mainMenuPermissions", mainMenuPermissions);
         }
      } else {
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

         MainMenuPermissions mainMenuPermissions = new MainMenuPermissions(displaySetup, displaySyncCourseRoster,
              displayDiscussInGoogleGroups, displayShareAndCollaborate, displayFolderWrapper, displayCourseFilesFolder,
              displayDropBoxFolder, displayMyDropBoxFolder, displayFileRepository, displayInstructorFilesFolder,
              displayCourseInformation);

         model.addAttribute("mainMenuPermissions", mainMenuPermissions);
      }

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

      boolean updatedSomething = false;
      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      String courseTitle = (String)request.getSession().getAttribute(Constants.COURSE_TITLE_KEY);
      List<String> errors = new ArrayList<>();

      String allGroupEmail = "";
      String teacherGroupEmail = "";

      // get some official group emails here to not call this repeatedly in multiple methods in googleCourseToolsService
      try {
         List<Group> groups = googleCourseToolsService.getGroupsForCourse(courseId);
         for (Group group : groups) {
            if (group.getEmail().contains("all")) {
               allGroupEmail = group.getEmail();
               break;
            } else if (group.getEmail().contains("teachers")) {
               teacherGroupEmail = group.getEmail();
               break;
            }
         }
      } catch (IOException e) {
         // something bad happened, so let's bail on it all
         errors.add("Error getting group info from Google. Bailing on setup changes.");
         model.addAttribute("setupErrors", errors);
         return index(courseId, model, request);
      }

      if (createCourseFileFolder) {
         try {
            courseInit.setCoursefilesFolderId(googleCourseToolsService.createCourseFileFolder(courseId, courseTitle, teacherGroupEmail).getId());
         } catch (IOException e) {
            String courseFilesFolderError = "Issue with creating the course file folder";
            errors.add(courseFilesFolderError);
            log.error(courseFilesFolderError, e);
         }
         updatedSomething = true;
      }

      if (createInstructorFileFolder) {
         try {
            courseInit.setInstructorFolderId(googleCourseToolsService.createInstructorFileFolder(courseId, courseTitle, allGroupEmail, teacherGroupEmail).getId());
         } catch (IOException e) {
            String instructorFolderError = "Issue with creating the instructor file folder";
            errors.add(instructorFolderError);
            log.error(instructorFolderError, e);
         }
         updatedSomething = true;
      }

      if (createDropboxFolder) {
         try {
            courseInit.setDropboxFolderId(googleCourseToolsService.createDropboxFolder(courseId, courseTitle).getId());
         } catch (IOException e) {
            String dropboxFolderError = "Issue with creating the dropbox file folder";
            errors.add(dropboxFolderError);
            log.error(dropboxFolderError, e);
         }
         updatedSomething = true;
      }

      if (createFileRepositoryFolder) {
         try {
            courseInit.setFileRepoId(googleCourseToolsService.createFileRepositoryFolder(courseId, courseTitle, allGroupEmail).getId());
         } catch (IOException e) {
            String fileRepoError = "Issue with creating the file repository folder";
            errors.add(fileRepoError);
            log.error(fileRepoError, e);
         }
         updatedSomething = true;
      }

      // TODO
      if (createMailingList) {
//         updatedSomething = true;
      }

      // any changes to TA or Designer access?
      if (courseInit.isTaTeacher() != taAccess || courseInit.isDeTeacher() != designerAccess) {
         // TODO
//         courseInit.setTaTeacher(taAccess);
//         courseInit.setDeTeacher(designerAccess);
//         updatedSomething = true;
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
