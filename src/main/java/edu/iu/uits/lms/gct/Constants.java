package edu.iu.uits.lms.gct;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

   String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
   String SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut";

   /**
    * Group types the tool will create
    */
   enum GROUP_TYPES {
      ALL,
      TEACHER
   }

   String DROPBOX_QUEUE = "gct_dropbox";

   @AllArgsConstructor
   @Getter
   enum FOLDER_TYPES {
      courseFiles("COURSE FILES"),
      instructorFiles("INSTRUCTOR FILES"),
      dropBox("DROP BOX"),
      fileRepository("FILE REPOSITORY");

      private String text;
   }

   /**
    * Group membership role definitions
    * TODO Is there a legit constant defined somewhere for this?
    */
   @AllArgsConstructor
   @Getter
   enum GROUP_ROLES {
      OWNER("Owner"),
      MANAGER("Manager"),
      MEMBER("Member"),
      COMMENTER("Commenter"),
      VIEWER("Viewer"),
      EDITOR("Editor"),
      READER("Reader"),
      WRITER("Writer");

      private String text;
   }

   enum PERMISSION_TYPE {
      GROUP,
      USER
   }
}
