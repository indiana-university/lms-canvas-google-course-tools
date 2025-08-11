package edu.iu.uits.lms.gct;

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
