package edu.iu.uits.lms.gct.controller;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.drive.model.File;
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

@Controller
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
   public ModelAndView createGroup(@PathVariable("courseId") String courseId, @RequestParam("groupName") String groupName,
                                   @RequestParam("groupEmail") String groupEmail,
                                   Model model, HttpSession httpSession) {
      try {
         Group group = googleCourseToolsService.createGroup(groupName, groupEmail);
         log.info("Group details: {}", group);
      } catch (IOException e) {
         log.error("uh oh", e);
      }

      return index(courseId, model, httpSession);
   }

   @PostMapping("/groups/{courseId}")
   @Secured(LtiAuthenticationProvider.LTI_USER_ROLE)
   public ModelAndView getGroups(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      try {
         List<Group> groups = googleCourseToolsService.getGroups();
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
}
