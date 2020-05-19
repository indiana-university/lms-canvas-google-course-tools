package edu.iu.uits.lms.gct.controller;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.drive.model.File;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
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

import javax.servlet.http.HttpSession;
import java.io.IOException;
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

   @RequestMapping("/index/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /index");
      LtiAuthenticationToken token = getValidatedToken(courseId);

      //For session tracking
      model.addAttribute("customId", httpSession.getId());
      model.addAttribute("courseId", courseId);
      return new ModelAndView("index");
   }

   @PostMapping("/createGroup/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView createCourseGroups(@PathVariable("courseId") String courseId, @RequestParam("allMembers") String allEmails,
                                          @RequestParam("teacherMembers") String teacherEmails, Model model, HttpSession httpSession) {
      LtiAuthenticationToken token = getValidatedToken(courseId);
      String courseTitle = (String)token.getData().get(Constants.COURSE_TITLE_KEY);
      try {
         Map<Constants.GROUP_TYPES, Group> groups = googleCourseToolsService.createCourseGroups(courseId, courseTitle, false);
         log.info("Group details: {}", groups);

         List<Member> allMembers = googleCourseToolsService.addMembersToGroup(groups.get(Constants.GROUP_TYPES.ALL).getEmail(), allEmails.split(","));
         log.info("All Membership details: {}", allMembers);

         List<Member> teacherMembers = googleCourseToolsService.addMembersToGroup(groups.get(Constants.GROUP_TYPES.TEACHER).getEmail(), teacherEmails.split(","));
         log.info("Teacher Membership details: {}", teacherMembers);

      } catch (IOException e) {
         log.error("uh oh", e);
      }

      return index(courseId, model, httpSession);
   }

   @PostMapping("/groups/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView getGroups(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      try {
         List<Group> groups = googleCourseToolsService.getGroupsForCourse(courseId);

         model.addAttribute("groups", groups);
//         log.info("Group details: {}", groups);
      } catch (IOException e) {
         log.error("uh oh", e);
      }

      return index(courseId, model, httpSession);
   }

   @PostMapping("/files/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView getFiles(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      try {
         List<File> files = googleCourseToolsService.getDriveFiles();
         model.addAttribute("files", files);
//         log.info("File details: {}", files);
      } catch (IOException e) {
         log.error("uh oh", e);
      }

      return index(courseId, model, httpSession);
   }

   @PostMapping("/folders/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView initFolders(@PathVariable("courseId") String courseId,
                                   @RequestParam("launchedUser") String launchedUser, Model model, HttpSession httpSession) {
      try {
         List<String> folderIds = googleCourseToolsService.initBaseFolders();
         File userFolder = googleCourseToolsService.createUserRootFolder(launchedUser + "@iu.edu", launchedUser);
//         model.addAttribute("files", files);
//         log.info("File details: {}", files);
      } catch (IOException e) {
         log.error("uh oh", e);
      }

      return index(courseId, model, httpSession);
   }
}
