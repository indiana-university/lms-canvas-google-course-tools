package edu.iu.uits.lms.gct.services;

import canvas.client.generated.api.CanvasApi;
import canvas.client.generated.api.ConversationsApi;
import canvas.client.generated.api.CoursesApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.PickerResponse;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static edu.iu.uits.lms.gct.Constants.FOLDER_MIME_TYPE;

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

      eligible = googleCourseToolsService.verifyUserEligibility("foo@bar.com", "foo", "1234");
      Assert.assertTrue(eligible);
   }

   @Test
   public void objectMapperTest() throws Exception {
      String json = "[{\"id\":\"0B5k7uIy6xQbMUWdSLW8zYXlpalU\",\"serviceId\":\"docs\",\"mimeType\":\"application/vnd.google-apps.folder\",\"name\":\"LMS Team\",\"description\":\"\",\"type\":\"folder\",\"lastEditedUtc\":1597405965912,\"iconUrl\":\"https://drive-thirdparty.googleusercontent.com/16/type/application/vnd.google-apps.folder+shared\",\"url\":\"https://drive.google.com/drive/folders/0B5k7uIy6xQbMUWdSLW8zYXlpalU\",\"embedUrl\":\"https://drive.google.com/embeddedfolderview?id=0B5k7uIy6xQbMUWdSLW8zYXlpalU\",\"driveSuccess\":false,\"driveError\":\"NETWORK\",\"sizeBytes\":0,\"parentId\":\"root\",\"isShared\":true},{\"id\":\"17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY\",\"serviceId\":\"docs\",\"mimeType\":\"image/jpeg\",\"name\":\"RHIT_Bonfire_ZoomBG.jpg\",\"description\":\"\",\"type\":\"photo\",\"lastEditedUtc\":1591794466917,\"iconUrl\":\"https://drive-thirdparty.googleusercontent.com/16/type/image/jpeg\",\"url\":\"https://drive.google.com/file/d/17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY/view?usp=drive_web\",\"embedUrl\":\"https://drive.google.com/file/d/17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY/preview?usp=drive_web\",\"driveSuccess\":false,\"driveError\":\"NETWORK\",\"sizeBytes\":933548,\"rotation\":0,\"rotationDegree\":0,\"parentId\":\"root\"},{\"id\":\"0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw\",\"serviceId\":\"DoclistBlob\",\"mimeType\":\"application/pdf\",\"name\":\"Getting started\",\"description\":\"\",\"type\":\"file\",\"lastEditedUtc\":1463606052288,\"iconUrl\":\"https://drive-thirdparty.googleusercontent.com/16/type/application/pdf\",\"url\":\"https://drive.google.com/file/d/0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw/view?usp=drive_web\",\"embedUrl\":\"https://drive.google.com/file/d/0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw/preview?usp=drive_web\",\"driveSuccess\":false,\"driveError\":\"NETWORK\",\"sizeBytes\":696774,\"parentId\":\"root\"}]";
      ObjectMapper mapper = new ObjectMapper();

      List<PickerResponse> pickerResponses = Arrays.asList(mapper.readValue(json, PickerResponse[].class));
      pickerResponses.sort(Comparator.comparing(PickerResponse::isFolder).reversed()
            .thenComparing(PickerResponse::getName));
//      Assert.assertNotNull("asdf");
      Assert.assertEquals("LMS Team", pickerResponses.get(0).getName());
      Assert.assertEquals("Getting started", pickerResponses.get(1).getName());
      Assert.assertEquals("RHIT_Bonfire_ZoomBG.jpg", pickerResponses.get(2).getName());


   }

   @TestConfiguration
   static class GoogleCourseToolsServiceTestContextConfiguration {
      @Bean
      public GoogleCourseToolsService googleCourseToolsService() {
         return new GoogleCourseToolsService();
      }

   }
}
