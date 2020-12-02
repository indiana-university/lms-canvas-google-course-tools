package edu.iu.uits.lms.gct.services;

import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.model.SerializableGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
public class CourseSessionUtilTest {

   @Test
   public void sessionUtilTest() throws Exception {
      String courseId = "1234";
      String courseId2 = "9876";
      HttpSession session = new MockHttpSession();

      CourseSessionUtil.addAttributeToSession(session, courseId2, Constants.COURSE_TITLE_KEY, "FOOBAR");
      CourseSessionUtil.addAttributeToSession(session, courseId, Constants.COURSE_TITLE_KEY, "ASDF");
      CourseSessionUtil.addAttributeToSession(session, courseId, Constants.USER_SIS_ID_KEY, "0123456789");

      String result = CourseSessionUtil.getAttributeFromSession(session, courseId, Constants.COURSE_TITLE_KEY, String.class);
      String result2 = CourseSessionUtil.getAttributeFromSession(session, courseId2, Constants.COURSE_TITLE_KEY, String.class);
      String result3 = CourseSessionUtil.getAttributeFromSession(session, courseId, Constants.USER_SIS_ID_KEY, String.class);
      String result4 = CourseSessionUtil.getAttributeFromSession(session, courseId, Constants.USER_EMAIL_KEY, String.class);
      Map<Constants.GROUP_TYPES, SerializableGroup> result5 = CourseSessionUtil.getAttributeFromSession(session, courseId, Constants.COURSE_GROUPS_KEY, Map.class);

      //More adds
      CourseSessionUtil.addAttributeToSession(session, courseId2, Constants.USER_EMAIL_KEY, "EMAIL!");
      String result6 = CourseSessionUtil.getAttributeFromSession(session, courseId2, Constants.USER_EMAIL_KEY, String.class);

      Assert.assertEquals("ASDF", result);
      Assert.assertEquals("FOOBAR", result2);
      Assert.assertEquals("0123456789", result3);
      Assert.assertNull(result4);
      Assert.assertNull(result5);
      Assert.assertEquals("EMAIL!", result6);


   }
}
