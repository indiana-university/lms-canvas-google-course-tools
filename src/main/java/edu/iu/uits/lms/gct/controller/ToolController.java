package edu.iu.uits.lms.gct.controller;

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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

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
            return new ModelAndView("setup");
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
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView setup(@PathVariable("courseId") String courseId, Model model) {
      model.addAttribute("courseId", courseId);

      return new ModelAndView("setup");
   }
}
