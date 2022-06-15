package edu.iu.uits.lms.gct.services;

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
