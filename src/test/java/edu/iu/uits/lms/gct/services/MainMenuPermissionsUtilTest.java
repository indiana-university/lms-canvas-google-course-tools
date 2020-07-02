package edu.iu.uits.lms.gct.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class MainMenuPermissionsUtilTest {

    @Test
    public void displaySetupTest() throws Exception {
        boolean isInstructor = true;
        Assert.assertTrue(MainMenuPermissionsUtil.displaySetup(isInstructor));

        isInstructor = false;
        Assert.assertFalse(MainMenuPermissionsUtil.displaySetup(isInstructor));
    }

//    @TestConfiguration
//    static class MainMenuPermissionsServiceTestContextConfiguration {
//        @Bean
//        public MainMenuPermissionsService mainMenuPermissionsService() {
//            return new MainMenuPermissionsService();
//        }
//
//        @Bean
//        public GoogleCourseToolsService googleCourseToolsService() {
//            return new GoogleCourseToolsService();
//        }
//
//        @Bean
//        public ToolConfig toolConfig() {
//            return new ToolConfig();
//        }
//
//        @Bean
//        public CourseInitRepository courseInitRepository() {
//            return new CourseInitRepository();
//        }
//    }
}
