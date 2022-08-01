package edu.iu.uits.lms.gct.services;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import edu.iu.uits.lms.gct.model.NotificationData;
import edu.iu.uits.lms.gct.model.SerializableGroup;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
public class FreemarkerTemplateTest {

   @Autowired
   private FreeMarkerConfigurer freeMarkerConfigurer;

   @Test
   public void testTemplateWithMailingList() throws Exception {
      Template freemarkerTemplate = freeMarkerConfigurer.getConfiguration()
            .getTemplate("courseInitializationNotification.ftlh");

      Map<String, Object> emailModel = new HashMap<>();
      emailModel.put("notificationData", buildNotificationData(true));

      String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, emailModel);

      String expectedBody = getResultBody("/notificationEmail1.txt");
      Assertions.assertEquals(expectedBody, body, "wrong body");
   }

   @Test
   public void testTemplateWithoutMailingList() throws Exception {
      Template freemarkerTemplate = freeMarkerConfigurer.getConfiguration()
            .getTemplate("courseInitializationNotification.ftlh");

      Map<String, Object> emailModel = new HashMap<>();
      emailModel.put("notificationData", buildNotificationData(false));

      String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, emailModel);

      String expectedBody = getResultBody("/notificationEmail2.txt");
      Assertions.assertEquals(expectedBody, body, "wrong body");
   }

   private NotificationData buildNotificationData(boolean includeMailingList) {
      NotificationData notificationData = new NotificationData();
      notificationData.setCourseTitle("Course Title!");

      SerializableGroup teacherGroup = new SerializableGroup();
      teacherGroup.setEmail("e@mail.com");
      teacherGroup.setName("Teacher Group");

      SerializableGroup allGroup = new SerializableGroup();
      allGroup.setEmail("all@mail.com");
      allGroup.setName("All Group");

      notificationData.setAllGroup(allGroup);
      notificationData.setTeacherGroup(teacherGroup);

      notificationData.setRootCourseFolder("Root Course Folder");
      notificationData.setCourseFilesFolder("Course Files Folder");
      notificationData.setGroupsFolder("Groups Files Folder");

      if (includeMailingList) {
         notificationData.setMailingListAddress("test@iu.edu");
         notificationData.setMailingListName("The Mailing List");
      }
      return notificationData;
   }

   private String getResultBody(String fileName) throws IOException {
      InputStream fileStream = this.getClass().getResourceAsStream(fileName);
      String result = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
      return result;
   }

   @TestConfiguration
   static class FreemarkerTemplateTestContextConfiguration {

      @Bean
      public FreeMarkerConfigurer freeMarkerConfigurer() {
         FreeMarkerConfigurer fmc = new FreeMarkerConfigurer();
         fmc.setTemplateLoaderPath("classpath:/templates");
         return fmc;
      }
   }
}
