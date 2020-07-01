package edu.iu.uits.lms.gct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static edu.iu.uits.lms.gct.services.GoogleCourseToolsService.FOLDER_MIME_TYPE;

@Slf4j
@RunWith(SpringRunner.class)
@Ignore
public class GoogleCourseToolsServiceTest {

//   @Autowired
//   private ToolConfig toolConfig;

   @Test
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

   @TestConfiguration
   static class GoogleCourseToolsServiceTestContextConfiguration {
//      @Bean
//      public EnrollmentClassificationService enrollmentClassificationService() {
//         return new EnrollmentClassificationService();
//      }
//
//      @Bean
//      public CourseListService courseListService() {
//         return new CourseListService();
//      }
   }
}
