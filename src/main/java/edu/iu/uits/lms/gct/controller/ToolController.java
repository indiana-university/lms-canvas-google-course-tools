package edu.iu.uits.lms.gct.controller;

import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.TokenInfo;
import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
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
      boolean isStudent = request.isUserInRole(LTIConstants.STUDENT_AUTHORITY);

      CourseInit courseInit = googleCourseToolsService.getCourseInit(courseId);
      String loginId = (String)token.getPrincipal();

      //For session tracking
//      model.addAttribute("customId", httpSession.getId());
//      model.addAttribute("courseInit", courseInit);
//      model.addAttribute("isInstructor", isInstructor);
//      model.addAttribute("isStudent", isStudent);
      model.addAttribute("courseId", courseId);

      boolean displaySetup = isInstructor;
      model.addAttribute("displaySetup", displaySetup);

      boolean displaySyncCourseRoster = isInstructor;
      model.addAttribute("displaySyncCourseRoster", displaySyncCourseRoster);

      boolean displayDiscussInGoogleGroups = courseInit.getMailingListAddress() != null;
      model.addAttribute("displayDiscussInGoogleGroups", displayDiscussInGoogleGroups);

      boolean displayShareAndCollaborate = displayShareAndCollaborate(request, courseInit);
      model.addAttribute("displayShareAndCollaborate", displayShareAndCollaborate);

      boolean displayFolderWrapper = displayFolderWrapper(request, courseInit);
      model.addAttribute("displayFolderWrapper", displayFolderWrapper);

      boolean displayCourseFilesFolder = courseInit.getCoursefilesFolderId() != null;
      model.addAttribute("displayCourseFilesFolder", displayCourseFilesFolder);

      boolean displayDropBoxFolder = displayDropBoxFolder(request, courseInit);
      model.addAttribute("displayDropBoxFolder", displayDropBoxFolder);

      boolean displayMyDropBoxFolder = isStudent && courseInit.getDropboxFolderId() != null;
      model.addAttribute("displayMyDropBoxFolder", displayMyDropBoxFolder);

      boolean displayFileRepository = courseInit.getFileRepoId() != null;
      model.addAttribute("displayFileRepository", displayFileRepository);

      boolean displayInstructorFilesFolder = displayInstructorFilesFolder(request, courseInit);
      model.addAttribute("displayInstructorFilesFolder", displayInstructorFilesFolder);

      boolean displayCourseInformation = displayCourseInformation(courseInit);
      model.addAttribute("displayCourseInformation", displayCourseInformation);

      // if ALL the display criteria is false, display the incomplete warning
      boolean displaySetupIncompleteWarning = !(displaySetup || displaySyncCourseRoster || displayDiscussInGoogleGroups ||
              displayShareAndCollaborate || displayFolderWrapper || displayCourseFilesFolder || displayDropBoxFolder ||
              displayMyDropBoxFolder || displayFileRepository || displayInstructorFilesFolder || displayCourseInformation);
      model.addAttribute("displaySetupIncompleteWarning", displaySetupIncompleteWarning);

      if (isInstructor && courseInit == null) {
         String courseTitle = (String)request.getSession().getAttribute(Constants.COURSE_TITLE_KEY);
         courseInit = googleCourseToolsService.initialize(courseId, courseTitle, loginId);
         if (courseInit != null) {
            return new ModelAndView("setup");
         } else {
            model.addAttribute("initError", "There were errors in the initialization process.");
         }
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

   // Only visible after at least one of the following folders has been created in Setup and is visible to the current role:
   // Instructor: Instructor Files, Course Files, Drop Boxes, Course Repo
   // Designer/TA if added to instructor group:  Instructor Files, Course Files, Drop Boxes, Course Repo
   // Designer/TA if not added to instructor group: Course Repo
   // Students: Course Repo, My Drop Box
   // Observers: Do not display to observers
   private boolean displayShareAndCollaborate(HttpServletRequest request, CourseInit courseInit) {
      if (courseInit == null) {
         // no init, return false
         return false;
      } else {
         boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
         boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
         boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
         boolean isStudent = request.isUserInRole(LTIConstants.STUDENT_AUTHORITY);

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
            if (courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null) {
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
   private boolean displayFolderWrapper(HttpServletRequest request, CourseInit courseInit) {
      if (courseInit == null) {
         // no init, return false
         return false;
      } else {
         boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
         boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
         boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
         boolean isStudent = request.isUserInRole(LTIConstants.STUDENT_AUTHORITY);
         boolean isObserver = request.isUserInRole(LTIConstants.OBSERVER_AUTHORITY);

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
            if (courseInit.getDropboxFolderId() != null || courseInit.getFileRepoId() != null || courseInit.getCoursefilesFolderId() != null) {
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

   // Only visible if Drop Boxes have been created in Setup.
   // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
   private boolean displayDropBoxFolder(HttpServletRequest request, CourseInit courseInit) {
      if (courseInit == null) {
         // no init, return false
         return false;
      } else if (courseInit.getDropboxFolderId() != null) {
         boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
         boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
         boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
         if (isInstructor || (isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
            return true;
         }
      }
      return false;
   }

   // Only visible if Instructor Files folder has been created in Setup.
   // Visible to instructors and optionally TAs/Designers if added to the Instructors group.
   private boolean displayInstructorFilesFolder(HttpServletRequest request, CourseInit courseInit) {
      if (courseInit == null) {
         // no init, return false
         return false;
      } else if (courseInit.getInstructorFolderId() != null) {
         boolean isInstructor = request.isUserInRole(LTIConstants.INSTRUCTOR_AUTHORITY);
         boolean isTa = request.isUserInRole(LTIConstants.TA_AUTHORITY);
         boolean isDesigner = request.isUserInRole(LTIConstants.DESIGNER_AUTHORITY);
         if (isInstructor || (isTa && courseInit.isTaTeacher()) || (isDesigner && courseInit.isDeTeacher())) {
            return true;
         }
      }
      return false;
   }

   // Only visible after a folder or mailing list has been created in Setup.
   private boolean displayCourseInformation(CourseInit courseInit) {
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
