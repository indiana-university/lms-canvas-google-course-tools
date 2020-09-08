package edu.iu.uits.lms.gct;

public interface Constants {

   /**
    * Key for token data
    */
   String COURSE_TITLE_KEY = "course_title";

   /**
    * Key for token data
    */
   String USER_EMAIL_KEY = "user_email";

   /**
    * Key for token data
    */
   String USER_SIS_ID_KEY = "user_sis_id";
   /**
    * Group types the tool will create
    */
   enum GROUP_TYPES {
      ALL,
      TEACHER
   }

   String DROPBOX_QUEUE = "gct_dropbox";
}
