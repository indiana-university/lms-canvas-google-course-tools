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
import com.google.api.services.admin.directory.model.Members;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.groupssettings.GroupssettingsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.GctProperty;
import edu.iu.uits.lms.gct.model.TokenInfo;
import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoogleCourseToolsService implements InitializingBean {

   private static final String APPLICATION_NAME = "LMS Google Course Tools";

   protected static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
   protected static final String SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut";

   protected static final String PROP_ROOT_FOLDER_KEY = "gct.rootFolderId";
   protected static final String PROP_COURSES_FOLDER_KEY = "gct.coursesFolderId";
   protected static final String PROP_USERS_FOLDER_KEY = "gct.usersFolderId";

   /**
    * For messing with drive
    */
   private Drive driveService;

   /**
    * For managing groups
    */
   private Directory directoryService;

   /**
    * For managing group settings
    */
   private Groupssettings groupsSettingsService;

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
   private enum GROUP_ROLES {
      OWNER,
      MANAGER,
      MEMBER
   }

   @Autowired
   private ToolConfig toolConfig;

   @Autowired
   private CourseInitRepository courseInitRepository;

   @Autowired
   private DropboxInitRepository dropboxInitRepository;

   @Autowired
   private UserInitRepository userInitRepository;

   @Autowired
   private GctPropertyRepository gctPropertyRepository;

   private static TokenInfo pickerTokenInfo;

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

            pickerTokenInfo = TokenInfo.builder()
                  .clientId(toolConfig.getPickerClientId())
                  .projectId(saCredentials.getProjectId())
                  .devKey(toolConfig.getPickerApiKey())
                  .build();

            log.debug("Client Id: {}", saCredentials.getClientId());
         }

         driveService = new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();

         directoryService = new Directory.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();

         groupsSettingsService = new Groupssettings.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();

      } catch (GeneralSecurityException | IOException e) {
         log.error("Unable to initialize service", e);
      }
   }

   public TokenInfo getPickerTokenInfo() {
      return pickerTokenInfo;
   }

   /**
    * Might need to become the end user to do things as/for them
    * @param user Email address for user to impersonate
    * @return
    */
   private Drive getDriveServiceAsUser(String user) {
      try {
         final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
         final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

         GoogleCredentials credentials;
         try (FileInputStream serviceAccountStream = new FileInputStream(CREDENTIALS_FILE_PATH)) {
            ServiceAccountCredentials saCredentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            credentials = saCredentials.createDelegated(user).createScoped(DriveScopes.DRIVE);
            log.debug("Client Id: {}", saCredentials.getClientId());
         }

         Drive driveService = new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
               .setApplicationName(APPLICATION_NAME)
               .build();
         return driveService;
      } catch (GeneralSecurityException | IOException e) {
         log.error("Unable to initialize service", e);
      }
      return null;
   }

   public File createCourseRootFolder(String courseId, String courseTitle, String emailForAccess) throws IOException {
      GctProperty coursesIdProp = gctPropertyRepository.findByKey(PROP_COURSES_FOLDER_KEY);
      String courseDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);

      //Make sure folder doesn't already exist for this parent
      String query = MessageFormat.format("name = '{0}' and parents in ({1}) and mimeType = '{2}'",
            toolConfig.getEnvDisplayPrefix() + courseDisplay, coursesIdProp.getValue(), FOLDER_MIME_TYPE);
      FileList fileList = driveService.files().list().setQ(query).setOrderBy("createdTime").execute();

      File courseFolder = null;

      if (fileList != null && fileList.getFiles() != null && fileList.getFiles().size() > 0) {
         log.warn("More than one course folder returned for this course.  Using the earliest one created.");
         courseFolder = fileList.getFiles().get(0);
      } else {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(toolConfig.getEnvDisplayPrefix() + courseDisplay);
         gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(coursesIdProp.getValue()));
         gctCourseMetadata.setDescription("Parent folder for shared folders belonging to the course " + courseDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         courseFolder = driveService.files().create(gctCourseMetadata)
               .setEnforceSingleParent(true)
               .execute();
      }

      log.info("User folder: {}", courseFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType("group");
      folderPermission.setRole("reader");
      folderPermission.setEmailAddress(emailForAccess);
      Permission permission = driveService.permissions().create(courseFolder.getId(), folderPermission)
            .setSendNotificationEmail(false)
            .execute();
      log.info("Folder permission: {}", permission);

      return courseFolder;
   }

   public File createUserRootFolder(String userEmail, String username) throws IOException {
      GctProperty usersIdProp = gctPropertyRepository.findByKey(PROP_USERS_FOLDER_KEY);
      File gctUserMetadata = new File();
      gctUserMetadata.setName(toolConfig.getEnvDisplayPrefix() + "Google Course Tools (" + username + ")");
      gctUserMetadata.setMimeType(FOLDER_MIME_TYPE);
      gctUserMetadata.setParents(Collections.singletonList(usersIdProp.getValue()));
      gctUserMetadata.setDescription("Parent folder for course folders created by the Google Course Tools app for Canvas.  Please do not move or delete.");
      gctUserMetadata.setWritersCanShare(false);

      File userFolder = driveService.files().create(gctUserMetadata)
            .setEnforceSingleParent(true)
            .execute();
      log.info("User folder: {}", userFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType("user");
      folderPermission.setRole("writer");
      folderPermission.setEmailAddress(userEmail);
      Permission permission = driveService.permissions().create(userFolder.getId(), folderPermission)
            .setSendNotificationEmail(false)
            .execute();
      log.info("Folder permission: {}", permission);

      //Create the shortcut
      File shortcut = new File();
      shortcut.setName(userFolder.getName());
      shortcut.setMimeType(SHORTCUT_MIME_TYPE);
      File.ShortcutDetails sd = new File.ShortcutDetails();
      sd.setTargetId(userFolder.getId());
      sd.setTargetMimeType(userFolder.getMimeType());
      shortcut.setShortcutDetails(sd);
      File shortcutFolder = getDriveServiceAsUser(userEmail).files().create(shortcut)
            .execute();
      log.info("Shortcut Info: {}", shortcutFolder);

      return userFolder;
   }

   public List<String> initBaseFolders() throws IOException {
      List<String> ids = new ArrayList<>();

      //Check for the existence of the required root folders.
      GctProperty rootIdProp = gctPropertyRepository.findByKey(PROP_ROOT_FOLDER_KEY);
      GctProperty coursesIdProp = gctPropertyRepository.findByKey(PROP_COURSES_FOLDER_KEY);
      GctProperty usersIdProp = gctPropertyRepository.findByKey(PROP_USERS_FOLDER_KEY);

      if (rootIdProp == null) {
         File rootFolderMetadata = new File();
         rootFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "Google Course Tools Admin");
         rootFolderMetadata.setMimeType(FOLDER_MIME_TYPE);
//      rootFolderMetadata.setShared(false);
         rootFolderMetadata.setDescription("Container folder for Google Course Tools Assets.");
         rootFolderMetadata.setWritersCanShare(false);
         File rootFolder = driveService.files().create(rootFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, shared, description, writersCanShare")
               .execute();
         log.info("Root folder info: {}", rootFolder);
         rootIdProp = new GctProperty(PROP_ROOT_FOLDER_KEY, rootFolder.getId());
         gctPropertyRepository.save(rootIdProp);
         ids.add(rootFolder.getId());
      }

      if (coursesIdProp == null) {
         File coursesFolderMetadata = new File();
         coursesFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "GCT Courses");
         coursesFolderMetadata.setMimeType(FOLDER_MIME_TYPE);
//      coursesFolderMetadata.setShared(false);
         coursesFolderMetadata.setDescription("Container for course folders created by the Google Course Tools LTI.");
         coursesFolderMetadata.setWritersCanShare(false);
         coursesFolderMetadata.setParents(Collections.singletonList(rootIdProp.getValue()));
         File coursesFolder = driveService.files().create(coursesFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, parents")
               .execute();
         log.info("Course folder info: {}", coursesFolder);
         coursesIdProp = new GctProperty(PROP_COURSES_FOLDER_KEY, coursesFolder.getId());
         gctPropertyRepository.save(coursesIdProp);
         ids.add(coursesFolder.getId());
      }

      if (usersIdProp == null) {
         File usersFolderMetadata = new File();
         usersFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "GCT User Folders");
         usersFolderMetadata.setMimeType(FOLDER_MIME_TYPE);
         usersFolderMetadata.setParents(Collections.singletonList(rootIdProp.getValue()));
//      usersFolderMetadata.setShared(false);
         usersFolderMetadata.setDescription("Container for user folders created by the Google Course Tools LTI.");
         usersFolderMetadata.setWritersCanShare(false);
         File usersFolder = driveService.files().create(usersFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, parents")
               .execute();
         log.info("Users folder info: {}", usersFolder);
         usersIdProp = new GctProperty(PROP_USERS_FOLDER_KEY, usersFolder.getId());
         gctPropertyRepository.save(usersIdProp);
         ids.add(usersFolder.getId());
      }

      return ids;
   }

   public List<File> getDriveFiles() throws IOException {
      // Print the names and IDs for up to 10 files.
      FileList result = driveService.files().list()
//            .setPageSize(10)
//            .setFields("nextPageToken, files(id, name)")
            .setOrderBy("name")
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

   public Map<Constants.GROUP_TYPES, Group> createCourseGroups(String canvasCourseId, String courseName, boolean mailingListActive) throws IOException {
      Map<Constants.GROUP_TYPES, Group> groups = new HashMap<>();
      groups.put(Constants.GROUP_TYPES.ALL, createAllGroup(canvasCourseId, courseName, mailingListActive));
      groups.put(Constants.GROUP_TYPES.TEACHER, createTeachersGroup(canvasCourseId, courseName));
      return groups;
   }

   /**
    *
    * @param canvasCourseId
    * @param courseName
    * @param mailingListActive
    * @return
    * @throws IOException
    */
   private Group createAllGroup(String canvasCourseId, String courseName, boolean mailingListActive) throws IOException {

      String email = canvasCourseId + "-all-iu-group@iu.edu";
      String groupName = courseName + "-" + canvasCourseId + " All";
      String groupDescription = "Google group for all members of " + courseName + "-" + canvasCourseId;

      Group group;
      try {
         //Look for an existing group
         group = getGroup(email);
      } catch (IOException e) {
         //Group doesn't exist.  Create it.
         Group newGroup = new Group();
         newGroup.setName(groupName);
         newGroup.setDescription(groupDescription);
         newGroup.setEmail(email);

         group = directoryService.groups().insert(newGroup).execute();

         com.google.api.services.groupssettings.model.Groups groupSettings = groupsSettingsService.groups().get(group.getEmail()).execute();
         //TODO Any chance there are constants for these somewhere?
         groupSettings.setWhoCanJoin("INVITED_CAN_JOIN");
         groupSettings.setWhoCanViewMembership("ALL_MANAGERS_CAN_VIEW");
         groupSettings.setWhoCanViewGroup("ALL_MEMBERS_CAN_VIEW");
         groupSettings.setAllowExternalMembers("true");
         groupSettings.setPrimaryLanguage("en");
         groupSettings.setArchiveOnly("false");
         groupSettings.setMessageModerationLevel("MODERATE_NONE");
         groupSettings.setSpamModerationLevel("REJECT");
         groupSettings.setReplyTo("REPLY_TO_LIST");
         groupSettings.setIncludeCustomFooter("false");
         groupSettings.setCustomFooterText(null);
         groupSettings.setSendMessageDenyNotification("true");
         groupSettings.setDefaultMessageDenyNotificationText("Your message was not accepted by the Google Group named " + groupName);
         groupSettings.setMembersCanPostAsTheGroup("false");
         groupSettings.setIncludeInGlobalAddressList("true");
         groupSettings.setWhoCanLeaveGroup("NONE_CAN_LEAVE");
         groupSettings.setWhoCanContactOwner("ALL_MANAGERS_CAN_CONTACT");
         groupSettings.setFavoriteRepliesOnTop("true");
         groupSettings.setWhoCanModerateMembers("OWNERS_AND_MANAGERS");
         groupSettings.setWhoCanModerateContent("OWNERS_AND_MANAGERS");
         groupSettings.setWhoCanAssistContent("OWNERS_AND_MANAGERS");
         groupSettings.setCustomRolesEnabledForSettingsToBeMerged("false");
         groupSettings.setEnableCollaborativeInbox("false");
         groupSettings.setWhoCanDiscoverGroup("ALL_MEMBERS_CAN_DISCOVER");

         if (mailingListActive) {
            groupSettings.setWhoCanPostMessage("ALL_MEMBERS_CAN_POST");
            groupSettings.setAllowWebPosting("true");
            groupSettings.setIsArchived("true");
         } else {
            groupSettings.setWhoCanPostMessage("ALL_OWNERS_CAN_POST");
            groupSettings.setAllowWebPosting("false");
            groupSettings.setIsArchived("false");
         }
         groupsSettingsService.groups().update(group.getEmail(), groupSettings).execute();

      }
      return group;
   }

   /**
    *
    * @param canvasCourseId
    * @param courseName
    * @return
    * @throws IOException
    */
   private Group createTeachersGroup(String canvasCourseId, String courseName) throws IOException {
      String email = canvasCourseId + "-teachers-iu-group@iu.edu";
      String groupName = courseName + "-" + canvasCourseId + " Teachers";
      String groupDescription = "Google group for instructors of " + courseName + "-" + canvasCourseId;

      Group group;
      try {
         //Look for an existing group
         group = getGroup(email);
      } catch (IOException e) {
         //Group doesn't exist.  Create it.
         Group newGroup = new Group();
         newGroup.setName(groupName);
         newGroup.setDescription(groupDescription);
         newGroup.setEmail(email);

         group = directoryService.groups().insert(newGroup).execute();

         com.google.api.services.groupssettings.model.Groups groupSettings = groupsSettingsService.groups().get(group.getEmail()).execute();
         //TODO Any chance there are constants for these somewhere?
         groupSettings.setWhoCanJoin("INVITED_CAN_JOIN");
         groupSettings.setWhoCanViewMembership("ALL_MANAGERS_CAN_VIEW");
         groupSettings.setWhoCanViewGroup("ALL_MEMBERS_CAN_VIEW");
         groupSettings.setAllowExternalMembers("true");
         groupSettings.setWhoCanPostMessage("ALL_OWNERS_CAN_POST");
         groupSettings.setAllowWebPosting("false");
         groupSettings.setPrimaryLanguage("en");
         groupSettings.setIsArchived("true");
         groupSettings.setArchiveOnly("false");
         groupSettings.setMessageModerationLevel("MODERATE_NONE");
         groupSettings.setSpamModerationLevel("REJECT");
         groupSettings.setReplyTo("REPLY_TO_LIST");
         groupSettings.setIncludeCustomFooter("false");
         groupSettings.setCustomFooterText(null);
         groupSettings.setSendMessageDenyNotification("true");
         groupSettings.setDefaultMessageDenyNotificationText("Your message was not accepted by the Google Group named " + groupName);
         groupSettings.setMembersCanPostAsTheGroup("false");
         groupSettings.setIncludeInGlobalAddressList("true");
         groupSettings.setWhoCanLeaveGroup("NONE_CAN_LEAVE");
         groupSettings.setWhoCanContactOwner("ALL_MANAGERS_CAN_CONTACT");
         groupSettings.setFavoriteRepliesOnTop("true");
         groupSettings.setWhoCanModerateMembers("OWNERS_AND_MANAGERS");
         groupSettings.setWhoCanModerateContent("OWNERS_AND_MANAGERS");
         groupSettings.setWhoCanAssistContent("OWNERS_AND_MANAGERS");
         groupSettings.setCustomRolesEnabledForSettingsToBeMerged("false");
         groupSettings.setEnableCollaborativeInbox("false");
         groupSettings.setWhoCanDiscoverGroup("ALL_MEMBERS_CAN_DISCOVER");

         groupsSettingsService.groups().update(group.getEmail(), groupSettings).execute();
      }
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

   public List<Group> getGroupsForCourse(String courseId) throws IOException {
      Groups groups = directoryService.groups().list()
            .setQuery("email:" + courseId + "-*")
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

   public List<Member> addMembersToGroup(String groupEmail, String[] emails, GROUP_ROLES role) throws IOException {
      List<Member> members = new ArrayList<>();
      for (String email : emails) {
         Member member = addMemberToGroup(groupEmail, email, role);
         members.add(member);
      }
      return members;
   }

   public List<Member> getMembersOfGroup(String groupEmail) throws IOException {
      Members members = directoryService.members().list(groupEmail)
            .execute();
      return members.getMembers();
   }

   /**
    * Adds a member to a group (or returns an existing member)
    * @param groupEmail
    * @param email
    * @param role
    * @return
    * @throws IOException
    */
   public Member addMemberToGroup(String groupEmail, String email, GROUP_ROLES role) throws IOException {
      Member member = directoryService.members().get(groupEmail, email).execute();
      if (member == null) {
         member = new Member();
         member.setEmail(email);
         member.setRole(role.name());
         return directoryService.members().insert(groupEmail, member).execute();
      }
      return member;
   }

   private String loginToEmail(String loginId) {
      return loginId + "@iu.edu";
   }

   public CourseInit getCourseInit(String courseId) {
      return courseInitRepository.findByCourseId(courseId);
   }

   public UserInit getUserInit(String loginId) {
      return userInitRepository.findByLoginId(loginId);
   }

   public CourseInit initialize(String courseId, String courseTitle, String loginId) {
      CourseInit ci = new CourseInit();
      UserInit ui = userInitRepository.findByLoginId(loginId);
      String userEmail = loginToEmail(loginId);
      try {
         Map<Constants.GROUP_TYPES, Group> groups = createCourseGroups(courseId, courseTitle, false);
         log.info("Group details: {}", groups);

         Member allMember = addMemberToGroup(groups.get(Constants.GROUP_TYPES.ALL).getEmail(), userEmail, GROUP_ROLES.MANAGER);
         log.info("All Membership details: {}", allMember);

         Member teacherMember = addMemberToGroup(groups.get(Constants.GROUP_TYPES.TEACHER).getEmail(), userEmail, GROUP_ROLES.MANAGER);
         log.info("Teacher Membership details: {}", teacherMember);

         File courseRootFolder = createCourseRootFolder(courseId, courseTitle, groups.get(Constants.GROUP_TYPES.ALL).getEmail());
         log.info("Course root folder: {}", courseRootFolder);

         ci.setCourseId(courseId);
//         ci.set

         if (ui == null) {
            File userRootFolder = createUserRootFolder(userEmail, loginId);
            ui = UserInit.builder()
                  .loginId(loginId)
                  .folderId(userRootFolder.getId())
                  .googleLoginId(userEmail)
                  .build();
            userInitRepository.save(ui);
         }

         addShortcut(courseRootFolder, ui.getFolderId());
         ci.setCourseFolderId(courseRootFolder.getId());


         courseInitRepository.save(ci);
      } catch (IOException e) {
         log.error("uh oh", e);
      }
      return ci;
   }

   private void addShortcut(File target, String parent) throws IOException {
      //Create the shortcut
      File shortcut = new File();
      shortcut.setName(target.getName());
      shortcut.setMimeType(SHORTCUT_MIME_TYPE);
      shortcut.setParents(Collections.singletonList(parent));
      File.ShortcutDetails sd = new File.ShortcutDetails();
      sd.setTargetId(target.getId());
      sd.setTargetMimeType(target.getMimeType());
      shortcut.setShortcutDetails(sd);
      File shortcutFolder = driveService.files().create(shortcut)
            .execute();
      log.info("Shortcut Info: {}", shortcutFolder);
   }

}
