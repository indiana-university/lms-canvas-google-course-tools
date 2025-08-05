package edu.iu.uits.lms.gct.services;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

   @MockitoBean
   private ToolConfig toolConfig;

   @MockitoBean
   private CourseInitRepository courseInitRepository;

   @MockitoBean
   private GroupsInitRepository groupsInitRepository;

   @MockitoBean
   private DropboxInitRepository dropboxInitRepository;

   @MockitoBean
   private UserInitRepository userInitRepository;

   @MockitoBean
   private GctPropertyRepository gctPropertyRepository;

   @MockitoBean
   private CourseService courseService;

   @MockitoBean
   private ConversationService conversationService;

   @MockitoBean
   private CanvasService canvasService;

   @MockitoBean
   private UserService userService;

   @MockitoBean
   private GroupService groupService;

   @MockitoBean
   private EmailService emailService;

   @MockitoBean
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
      Assertions.assertEquals("foo-iu-group", results);
   }

}
