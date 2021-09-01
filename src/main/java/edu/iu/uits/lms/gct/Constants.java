package edu.iu.uits.lms.gct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public interface Constants {

   /**
    * Key for token data
    */
   String COURSE_TITLE_KEY = "course_title";

   /**
    * Key for token data
    */
   String COURSE_SIS_ID_KEY = "course_sis_id";

   /**
    * Key for token data
    */
   String COURSE_CODE_KEY = "course_code";

   /**
    * Key for token data
    */
   String USER_EMAIL_KEY = "user_email";

   /**
    * Key for token data
    */
   String USER_SIS_ID_KEY = "user_sis_id";

   /**
    * Key for token data
    */
   String COURSE_GROUPS_KEY = "course_groups_key";

   String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
   String SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut";

   /**
    * Constant defining the ehcache provider
    */
   String EHCACHE_PROVIDER_TYPE = "org.ehcache.jsr107.EhcacheCachingProvider";

   String CACHE_DRIVE_SERVICE = "driveServiceAsUser";

   /**
    * Constant defining the max length for a Google group name
    */
   int GROUP_NAME_MAX_LENGTH = 73;

   /**
    * Group types the tool will create
    */
   enum GROUP_TYPES {
      ALL,
      TEACHER,
      CANVAS
   }

   @RequiredArgsConstructor
   @Getter
   @ToString
   enum FOLDER_TYPES {
      courseFiles("COURSE FILES"),
      instructorFiles("INSTRUCTOR FILES"),
      groupsFiles("GROUP FILES"),
      dropBoxes("DROP BOXES"),
      mydropBox("MY DROP BOX"),
      fileRepository("FILE REPOSITORY"),
      canvasCourseGroup("SHOULD NEVER SEE THIS TEXT");

      @NonNull
      @Setter
      private String text;

      @Setter
      private String folderId;
   }

   /**
    * Group membership role definitions
    * Need to be UPPER CASE for google's api
    * TODO Is there a legit constant defined somewhere for this?
    */
   @AllArgsConstructor
   @Getter
   enum GROUP_ROLES {
      OWNER("Owner"),
      MANAGER("Manager"),
      MEMBER("Member");

      private String text;
   }

   /**
    * File permission role definitions
    * Need to be LOWER CASE for google's api
    * TODO Is there a legit constant defined somewhere for this?
    */
   @AllArgsConstructor
   @Getter
   enum PERMISSION_ROLES {
      commenter("Commenter"),
      reader("Viewer"),
      writer("Editor");

      private String text;
   }

   enum PERMISSION_TYPE {
      group,
      user
   }
}
