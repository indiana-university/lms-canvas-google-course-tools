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

import edu.iu.uits.lms.gct.mailinglist.MxRecord;
import edu.iu.uits.lms.gct.mailinglist.MxRecordService;
import edu.iu.uits.lms.gct.mailinglist.MxRecordServicesEnvironmentConfig;
import edu.iu.uits.lms.gct.mailinglist.MxServicesConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Disabled
public class MxRecordServiceTest {
   @Autowired
   private MxRecordService mxRecordService;

   @Autowired
   @Qualifier("mxRestTemplate")
   private RestTemplate mxRestTemplate;

   @Autowired
   private MxServicesConfig config;

   @Test
   public void testGetterForExisting() {
      MxRecord mxRecord = mxRecordService.getMxRecord("chmaurer-foobar");
      Assertions.assertNotNull(mxRecord);
      Assertions.assertEquals(MxRecord.RESULT_SUCCESS, mxRecord.getResult());
   }

   @Test
   public void testGetterForNonExisting() {
      MxRecord mxRecord = mxRecordService.getMxRecord("chmaurer-foobar-7777777777");
      Assertions.assertNotNull(mxRecord);
      Assertions.assertEquals(MxRecord.RESULT_FAILED, mxRecord.getResult());
   }

   @Test
   public void testCreate() {
      MxRecord mxRecord = mxRecordService.createMxRecord("chmaurer-foobar");
      Assertions.assertNotNull(mxRecord);
      Assertions.assertEquals(MxRecord.RESULT_SUCCESS, mxRecord.getResult());
   }

   @TestConfiguration
   @Import(MxRecordServicesEnvironmentConfig.class)
   static class GoogleCourseToolsServiceTestContextConfiguration {

      @Value("${gct.mx.token.ci}")
      private String token;

      @Value("${gct.mx.baseUrl.ci}")
      private String baseUrl;

      @Bean
      public MxRecordService mxRecordService() {
         return new MxRecordService();
      }

      @Bean
      public MxServicesConfig mxServicesConfig() {
         MxServicesConfig config = new MxServicesConfig();
         config.setBaseUrl(baseUrl);
         config.setToken(token);
         return config;
      }

   }
}
