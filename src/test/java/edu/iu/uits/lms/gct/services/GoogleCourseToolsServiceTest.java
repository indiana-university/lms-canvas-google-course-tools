package edu.iu.uits.lms.gct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.canvas.services.ConversationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.GroupService;
import edu.iu.uits.lms.canvas.services.UserService;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.GroupsInitRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Collections;

import static edu.iu.uits.lms.gct.Constants.FOLDER_MIME_TYPE;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes={GoogleCourseToolsService.class})
@SpringBootTest
@Slf4j
public class GoogleCourseToolsServiceTest {

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @MockBean
   private ToolConfig toolConfig;

   @MockBean
   private CourseInitRepository courseInitRepository;

   @MockBean
   private GroupsInitRepository groupsInitRepository;

   @MockBean
   private DropboxInitRepository dropboxInitRepository;

   @MockBean
   private UserInitRepository userInitRepository;

   @MockBean
   private GctPropertyRepository gctPropertyRepository;

   @MockBean
   private CourseService courseService;

   @MockBean
   private ConversationService conversationService;

   @MockBean
   private CanvasService canvasService;

   @MockBean
   private UserService userService;

   @MockBean
   private GroupService groupService;

   @MockBean
   private EmailService emailService;

   @MockBean
   private FreeMarkerConfigurer freemarkerConfigurer;

   @Test
   @Disabled
   public void testFolderCreate() throws Exception {
      String username = "chmaurer";
      String userEmail = username + "@iu.edu";

//      String usersFolderId = toolConfig.getUsersFolderId();
      String usersFolderId = "1ZreeUlo-AnvLEBlpMmsmOJ9MueJSg_sS";

      File gctUserMetadata = new File();
      gctUserMetadata.setName("Google Course Tools (" + username + ")");
      gctUserMetadata.setMimeType(FOLDER_MIME_TYPE);
      gctUserMetadata.setParents(Collections.singletonList(usersFolderId));
      gctUserMetadata.setDescription("Parent folder for course folders created by the Google Course Tools app for Canvas.  Please do not move or delete.");
      gctUserMetadata.setWritersCanShare(false);

      Permission folderPermission = new Permission();
      folderPermission.setType("user");
      folderPermission.setRole("writer");
      folderPermission.setEmailAddress(userEmail);
//      folderPermission.set
      gctUserMetadata.setPermissions(Collections.singletonList(folderPermission));

//      log.info(gctUserMetadata.toPrettyString());

      ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

      String json = objectMapper.writeValueAsString(gctUserMetadata);
      log.info("json: {}", json);
   }

   @Test
   public void testUserVerification() {
      boolean eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "asdf", "01234");
      Assertions.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "asdf", "1234");
      Assertions.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "asdf", "4321");
      Assertions.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "qwerty", "4321");
      Assertions.assertFalse(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("foo@bar.com", "foo", "4321");
      Assertions.assertFalse(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("foo@bar.com", "foo", "2222222222");
      Assertions.assertTrue(eligible);
   }

   @Test
   public void testGroupName() {
      when(toolConfig.getEnvDisplayPrefix()).thenReturn("CI-");
      String groupNamePatternForAll = "{0}{1}-{2} All";
      String name = googleCourseToolsService.buildValidatedGroupName("1234567", "This is a pretty standard course name", groupNamePatternForAll);
      Assertions.assertEquals(52, name.length());

      name = googleCourseToolsService.buildValidatedGroupName("1234567", "This is a pretty standard course name.  But now here is some more.  Is it more than we can handle?", groupNamePatternForAll);
      Assertions.assertEquals(73, name.length());
   }

   @Test
   public void testEmailStripping() {
      String results = googleCourseToolsService.stripEmailDomain("foo-iu-group@iu.edu");
      Assertions.assertEquals("foo", results);
   }

}
