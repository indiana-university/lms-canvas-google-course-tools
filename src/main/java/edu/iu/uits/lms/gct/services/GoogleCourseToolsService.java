package edu.iu.uits.lms.gct.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.groupssettings.GroupssettingsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import edu.iu.uits.lms.gct.config.ToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class GoogleCourseToolsService implements InitializingBean {

   private static final String APPLICATION_NAME = "LMS Google Course Tools";

   /**
    * For messing with drive
    */
   private Drive driveService;

   /**
    * For managing groups
    */
   private Directory directoryService;

   /**
    * Global instance of the scopes required by this quickstart.
    * If modifying these scopes, delete your previously saved tokens/ folder.
    */
   private static final List<String> SCOPES = Arrays.asList(
         DriveScopes.DRIVE,
         DirectoryScopes.ADMIN_DIRECTORY_GROUP,
         GroupssettingsScopes.APPS_GROUPS_SETTINGS);
         //DirectoryScopes.ADMIN_DIRECTORY_USER);

   private static final String CREDENTIALS_FILE_PATH = "/conf/gct-creds.json";

   /**
    * Group membership role definitions
    * TODO Is there a legit constant defined somewhere for this?
    */
   private static enum GROUP_ROLES {
      OWNER,
      MANAGER,
      MEMBER
   }

   @Autowired
   private ToolConfig toolConfig;


   @Override
   public void afterPropertiesSet() {
      try {
         final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
         final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

         GoogleCredentials credentials;
         try (FileInputStream serviceAccountStream = new FileInputStream(CREDENTIALS_FILE_PATH)) {
            ServiceAccountCredentials saCredentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
//            saCredentials.getAccessToken();
            credentials = saCredentials.createDelegated(toolConfig.getImpersonationAccount()).createScoped(SCOPES);
            log.debug("Client Id: {}", saCredentials.getClientId());
         }

         driveService = new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();

         directoryService = new Directory.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();

      } catch (GeneralSecurityException | IOException e) {
         log.error("Unable to initialize service", e);
      }
   }

   public List<File> getDriveFiles() throws IOException {
      // Print the names and IDs for up to 10 files.
      FileList result = driveService.files().list()
            .setPageSize(10)
            .setFields("nextPageToken, files(id, name)")
            .execute();
      List<File> files = result.getFiles();
      if (files == null || files.isEmpty()) {
         System.out.println("No files found.");
      } else {
         System.out.println("Files:");
         for (File file : files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId());
         }
      }
      return files;
   }

   /**
    *
    * @param name
    * @param email
    * @return
    * @throws IOException
    */
   public Group createGroup(String name, String email) throws IOException {
      Group newGroup = new Group();
      newGroup.setName(name);
      newGroup.setDescription(name + " description");
      newGroup.setEmail(email);
      Group group = directoryService.groups().insert(newGroup).execute();
      return group;
   }

   public Group getGroup(String key) throws IOException {
      Group group = directoryService.groups().get(key).execute();
      return group;
   }

   public List<Group> getGroups() throws IOException {
      Groups groups = directoryService.groups().list()
//            .setMaxResults(10)
            .setDomain(toolConfig.getDomain())
            .execute();
      return groups.getGroups();
   }

   public List<User> getUsers() throws IOException {
      Users users = directoryService.users().list()
            .setDomain(toolConfig.getDomain())
            .execute();
      return users.getUsers();
   }

   public void addMembersToGroups() throws IOException {
      Member member = new Member();
      member.setEmail("chmaurer@iu.edu");
      member.setRole(GROUP_ROLES.MEMBER.name());

      directoryService.members().insert("", member);
//      Group group = getGroup();
//      group.
//      group.m

//      directoryService.groups().get("asdf").execute().
   }

}
