package edu.iu.uits.lms.gct.services;

import canvas.client.generated.api.CanvasApi;
import canvas.client.generated.api.ConversationsApi;
import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.GroupsApi;
import canvas.client.generated.api.UsersApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.GroupsInitRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import email.client.generated.api.EmailApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Collections;

import static edu.iu.uits.lms.gct.Constants.FOLDER_MIME_TYPE;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
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
   private CoursesApi coursesApi;

   @MockBean
   private ConversationsApi conversationsApi;

   @MockBean
   private CanvasApi canvasApi;

   @MockBean
   private UsersApi usersApi;

   @MockBean
   private GroupsApi groupsApi;

   @MockBean
   private EmailApi emailApi;

   @MockBean
   private FreeMarkerConfigurer freemarkerConfigurer;

   @Test
   @Ignore
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
      Assert.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "asdf", "1234");
      Assert.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "asdf", "4321");
      Assert.assertTrue(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("asdf@iu.edu", "qwerty", "4321");
      Assert.assertFalse(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("foo@bar.com", "foo", "4321");
      Assert.assertFalse(eligible);

      eligible = googleCourseToolsService.verifyUserEligibility("foo@bar.com", "foo", "2222222222");
      Assert.assertTrue(eligible);
   }

   @Test
   public void testGroupName() {
      when(toolConfig.getEnvDisplayPrefix()).thenReturn("CI-");
      String groupNamePatternForAll = "{0}{1}-{2} All";
      String name = googleCourseToolsService.buildValidatedGroupName("1234567", "This is a pretty standard course name", groupNamePatternForAll);
      Assert.assertEquals(52, name.length());

      name = googleCourseToolsService.buildValidatedGroupName("1234567", "This is a pretty standard course name.  But now here is some more.  Is it more than we can handle?", groupNamePatternForAll);
      Assert.assertEquals(73, name.length());
   }

   @Test
   public void testEmailStripping() {
      String results = googleCourseToolsService.stripEmailDomain("foo-iu-group@iu.edu");
      Assert.assertEquals("foo", results);
   }

   @TestConfiguration
   static class GoogleCourseToolsServiceTestContextConfiguration {
      @Bean
      public GoogleCourseToolsService googleCourseToolsService() {
         return new GoogleCourseToolsService();
      }

   }
}
