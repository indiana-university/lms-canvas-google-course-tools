package edu.iu.uits.lms.gct.services;

import canvas.client.generated.api.CanvasApi;
import canvas.client.generated.api.ConversationsApi;
import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.ConversationCreateWrapper;
import canvas.client.generated.model.User;
import canvas.helpers.EnrollmentHelper;
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
import com.google.api.services.admin.directory.model.MembersHasMember;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.groupssettings.GroupssettingsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.model.GctProperty;
import edu.iu.uits.lms.gct.model.NotificationData;
import edu.iu.uits.lms.gct.model.TokenInfo;
import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

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
import java.util.stream.Collectors;

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

   private static final String CREDENTIALS_FILE_PATH = "/usr/src/app/config/gct-creds.json";

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

   @Autowired
   private CoursesApi coursesApi;

   @Autowired
   private ConversationsApi conversationsApi;

   @Autowired
   private CanvasApi canvasApi;

   @Autowired
   private FreeMarkerConfigurer freemarkerConfigurer;

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

         //Make sure that the base folders have been initialized
         initBaseFolders();

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
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + MessageFormat.format("{0} ({1})", courseTitle, courseId);

      File courseFolder = findFolder(courseDisplay, coursesIdProp.getValue());

      if (courseFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
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

      //Create the shortcut to the user's root
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

      String email = toolConfig.getEnvDisplayPrefix() + canvasCourseId + "-all-iu-group@iu.edu";
      String groupName = toolConfig.getEnvDisplayPrefix() + courseName + "-" + canvasCourseId + " All";
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

         // this is a default in all groups created by our tool
         addMemberToGroup(email, toolConfig.getImpersonationAccount(), GROUP_ROLES.OWNER);

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
      String email = toolConfig.getEnvDisplayPrefix() + canvasCourseId + "-teachers-iu-group@iu.edu";
      String groupName = toolConfig.getEnvDisplayPrefix() + courseName + "-" + canvasCourseId + " Teachers";
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

         // this is a default in all groups created by our tool
         addMemberToGroup(email, toolConfig.getImpersonationAccount(), GROUP_ROLES.OWNER);
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

   public Map<Constants.GROUP_TYPES, Group> getGroupsForCourse(String courseId) throws IOException {
      Groups groups = directoryService.groups().list()
            .setQuery("email:" + toolConfig.getEnvDisplayPrefix() + courseId + "-*")
            .setDomain(toolConfig.getDomain())
            .execute();

      Map<Constants.GROUP_TYPES, Group> groupMap = new HashMap<>();

      for (Group group : groups.getGroups()) {
         if (group.getEmail().contains("all")) {
            groupMap.put(Constants.GROUP_TYPES.ALL, group);
         } else if (group.getEmail().contains("teachers")) {
            groupMap.put(Constants.GROUP_TYPES.TEACHER, group);
         }
      }
      return groupMap;
   }

   public List<com.google.api.services.admin.directory.model.User> getUsers() throws IOException {
      Users users = directoryService.users().list()
            .setDomain(toolConfig.getDomain())
            .execute();
      return users.getUsers();
   }

   public List<Member> addMembersToGroup(String groupEmail, String[] userEmails, GROUP_ROLES role) throws IOException {
      List<Member> members = new ArrayList<>();
      for (String userEmail : userEmails) {
         Member member = addMemberToGroup(groupEmail, userEmail, role);
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
    * @param userEmail
    * @param role
    * @return
    * @throws IOException
    */
   public Member addMemberToGroup(String groupEmail, String userEmail, GROUP_ROLES role) throws IOException {
      Member member;
      try {
         member = directoryService.members().get(groupEmail, userEmail).execute();
      } catch (IOException io) {
         member = new Member();
         member.setEmail(userEmail);
         member.setRole(role.name());
         return directoryService.members().insert(groupEmail, member).execute();
      }
      return member;
   }

   public void removeMemberFromGroup(String groupEmail, String userEmail) throws IOException {
      MembersHasMember hasMember = directoryService.members().hasMember(groupEmail, userEmail).execute();
      if (hasMember.getIsMember()) {
         directoryService.members().delete(groupEmail, userEmail).execute();
      }
   }

   private String loginToEmail(String loginId) {
      return loginId + "@iu.edu";
   }

   public CourseInit getCourseInit(String courseId) {
      return courseInitRepository.findByCourseId(courseId);
   }

   public DropboxInit getDropboxInit(String courseId, String loginId) {
      return dropboxInitRepository.findByCourseIdAndLoginId(courseId, loginId);
   }

   public UserInit getUserInit(String loginId) {
      return userInitRepository.findByLoginId(loginId);
   }

   /**
    * Do initialization for a course.  It is assumed that the loginId is for an instructor.
    * @param courseId
    * @param courseTitle
    * @param loginId
    * @return
    */
   public CourseInit courseInitialization(String courseId, String courseTitle, String loginId) {
      try {
         CourseInit ci = new CourseInit();

         //Create the course groups
         Map<Constants.GROUP_TYPES, Group> groups = createCourseGroups(courseId, courseTitle, false);
         log.info("Group details: {}", groups);

         //Create the root folder for this course
         File courseRootFolder = createCourseRootFolder(courseId, courseTitle, groups.get(Constants.GROUP_TYPES.ALL).getEmail());
         log.info("Course root folder: {}", courseRootFolder);

         ci.setCourseId(courseId);
         ci.setCourseFolderId(courseRootFolder.getId());

         courseInitRepository.save(ci);
         return ci;
      } catch (IOException e) {
         log.error("uh oh", e);
      }
      return null;
   }

   private void addShortcut(String targetId, String parent) throws IOException {
      File folder = driveService.files().get(targetId).execute();
      addShortcut(folder, parent);
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

   public void saveCourseInit(CourseInit courseInit) {
      courseInitRepository.save(courseInit);
   }

   public File createCourseFileFolder(String courseId, String courseTitle, String teacherGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseId(courseId).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": COURSE FILES";

      File courseFileFolder = findFolder(courseDisplay, courseFolderId);

      if (courseFileFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(courseFolderId));
         gctCourseMetadata.setDescription("Folder for sharing files with members of " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         courseFileFolder = driveService.files().create(gctCourseMetadata)
                 .setEnforceSingleParent(true)
                 .execute();
      }

      log.info("Course files folder: {}", courseFileFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType("group");
      folderPermission.setRole("writer");
      folderPermission.setEmailAddress(teacherGroupEmail);
      Permission permission = driveService.permissions().create(courseFileFolder.getId(), folderPermission)
              .setSendNotificationEmail(false)
              .execute();
      log.info("Course files folder permission: {}", permission);

      return courseFileFolder;
   }

   public File createInstructorFileFolder(String courseId, String courseTitle, String allGroupEmail, String teacherGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseId(courseId).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": INSTRUCTOR FILES";

      File instructorFileFolder = findFolder(courseDisplay, courseFolderId);

      if (instructorFileFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(courseFolderId));
         gctCourseMetadata.setDescription("Folder for sharing files with instructors of " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         instructorFileFolder = driveService.files().create(gctCourseMetadata)
                 .setEnforceSingleParent(true)
                 .execute();
      }

      log.info("Instructor files folder: {}", instructorFileFolder);

      deleteFolderPermission(instructorFileFolder.getId(), allGroupEmail);

      Permission folderPermission = new Permission();
      folderPermission.setType("group");
      folderPermission.setRole("writer");
      folderPermission.setEmailAddress(teacherGroupEmail);
      Permission permission = driveService.permissions().create(instructorFileFolder.getId(), folderPermission)
              .setSendNotificationEmail(false)
              .execute();
      log.info("Instructor files folder permission: {}", permission);

      return instructorFileFolder;
   }

   public File createDropboxFolder(String courseId, String courseTitle) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseId(courseId).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": DROP BOXES";

      File dropBoxFolder = findFolder(courseDisplay, courseFolderId);

      if (dropBoxFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(courseFolderId));
         gctCourseMetadata.setDescription("Parent folder for student drop boxes in " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         dropBoxFolder = driveService.files().create(gctCourseMetadata)
                 .setEnforceSingleParent(true)
                 .execute();
      }

      log.info("Drop box folder: {}", dropBoxFolder);

      return dropBoxFolder;
   }

   public void createStudentDropboxFolders(String courseId, String courseTitle, String dropboxFolderId,
                                             String allGroupEmail, String teacherGroupEmail) throws IOException {
      // Get all active students from canvas
      List<User> students = coursesApi.getUsersForCourseByType(courseId,
            Collections.singletonList(EnrollmentHelper.TYPE.student.name()),
            Collections.singletonList(EnrollmentHelper.STATE.active.name()));

      //Get the last item in the list so we know when we get to it
//      User last = students.get(students.size()-1);

      // Run in batches of 100, as that's all google apis will support
//      List<List<User>> batches = ListUtils.partition(students, 100);
//      for (List<User> studentBatch : batches) {
//         BatchRequest batch = driveService.batch();
//         boolean batchNotEmpty = false;
      for (User student : students) {
         createStudentDropboxFolder(courseId, courseTitle, dropboxFolderId, student, allGroupEmail, teacherGroupEmail); //, batch) || batchNotEmpty;
      }
         //Don't want to run an empty batch - google hates that!
//         if (batchNotEmpty) {
//            batch.execute();
//         }
      // Send notification upon completion
      sendDropboxNotification(courseId, courseTitle, dropboxFolderId);
//      }
   }

   /**
    * Send dropbox notification
    * @param courseId
    * @param courseTitle
    * @param dropboxFolderId
    * @throws IOException
    */
   private void sendDropboxNotification(String courseId, String courseTitle, String dropboxFolderId) throws IOException {
      List<User> courseInstructors = coursesApi.getUsersForCourseByType(courseId,
            Collections.singletonList(EnrollmentHelper.TYPE.teacher.name()),
            null);

      List<String> courseInstructorIds = courseInstructors.stream()
            .map(User::getId)
            .collect(Collectors.toList());

      String courseLink = MessageFormat.format("{0}/courses/{1}", canvasApi.getBaseUrl(), courseId);
      File dbFolder = driveService.files().get(dropboxFolderId).setFields("id,webViewLink").execute();
      String dbLink = dbFolder.getWebViewLink();

      Map<String, Object> emailModel = new HashMap<>();
      emailModel.put("courseTitle", courseTitle);
      emailModel.put("courseLink", courseLink);
      emailModel.put("dropboxFolderLink", dbLink);

      ConversationCreateWrapper wrapper = new ConversationCreateWrapper();
      wrapper.setRecipients(courseInstructorIds);
      wrapper.setContextCode("course_" + courseId);
      wrapper.setGroupConversation(true);
      wrapper.setSubject("Your Google Course Tools Drop Boxes are ready");
      sendNotification("dropbox.ftlh", emailModel, wrapper);

   }

   public void sendCourseSetupNotification(CourseInit courseInit, NotificationData notificationData) {
      String courseId = courseInit.getCourseId();
      List<User> courseInstructors = coursesApi.getUsersForCourseByType(courseId,
            Collections.singletonList(EnrollmentHelper.TYPE.teacher.name()),
            null);

      List<String> courseInstructorIds = courseInstructors.stream()
            .map(User::getId)
            .collect(Collectors.toList());

      Map<String, Object> emailModel = new HashMap<>();
      emailModel.put("notificationData", notificationData);

      ConversationCreateWrapper wrapper = new ConversationCreateWrapper();
      wrapper.setRecipients(courseInstructorIds);
      wrapper.setContextCode("course_" + courseId);
      wrapper.setGroupConversation(true);
      wrapper.setSubject("Google Course Tools for " + notificationData.getCourseTitle());

      sendNotification("courseInitializationNotification.ftlh", emailModel, wrapper);
   }

   /**
    * Send notification
    * @param templateName
    * @param emailModel
    * @param wrapper
    */
   private void sendNotification(String templateName, Map<String, Object> emailModel, ConversationCreateWrapper wrapper) {
      try {
         Template freemarkerTemplate = freemarkerConfigurer.createConfiguration()
               .getTemplate(templateName);

         String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, emailModel);

         wrapper.setBody(body);
         conversationsApi.postConversation(wrapper, null, null);
      } catch (TemplateException | IOException e) {
         log.error("Unable to send dropbox notification email", e);
      }
   }

//   @Builder
//   private static class DropboxFolderCreationCallback extends JsonBatchCallback<File> {
//
//      private String loginId;
//      private String courseId;
//      private String allGroupEmail;
//      private String teacherGroupEmail;
//      private boolean isLast;
//      private GoogleCourseToolsService gctService;
//
//      @Override
//      public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) throws IOException {
//         log.error("Error creating student dropbox for " + loginId + " and course " + courseId);
//      }
//
//      @Override
//      public void onSuccess(File file, HttpHeaders httpHeaders) throws IOException {
//         String createdFolderId = file.getId();
//         String studentEmail = loginId + "@iu.edu";
//
//         log.info("Created dropbox folder for " + loginId + " and course " + courseId);
//         log.info(teacherGroupEmail);
//         log.info(allGroupEmail);
//
//         //Do the permissions
//         gctService.deleteFolderPermission(createdFolderId, allGroupEmail);
//
//         Permission studentPerm = new Permission();
//         studentPerm.setType("user");
//         studentPerm.setRole("writer");
//         studentPerm.setEmailAddress(studentEmail);
//         Permission studPermission = gctService.driveService.permissions().create(createdFolderId, studentPerm)
//               .setSendNotificationEmail(false)
//               .execute();
//
//         Permission teacherPerm = new Permission();
//         teacherPerm.setType("group");
//         teacherPerm.setRole("writer");
//         teacherPerm.setEmailAddress(teacherGroupEmail);
//         Permission teachPermission = gctService.driveService.permissions().create(createdFolderId, teacherPerm)
//               .setSendNotificationEmail(false)
//               .execute();
//
//
//         //Save the init stuff
//         DropboxInit dropboxInit = DropboxInit.builder()
//               .loginId(loginId)
//               .googleLoginId(studentEmail)
//               .courseId(courseId)
//               .folderId(createdFolderId).build();
//         gctService.dropboxInitRepository.save(dropboxInit);
//      }
//   }

   private boolean createStudentDropboxFolder(String courseId, String courseTitle, String dropboxFolderId,
                                           User student, String allGroupEmail,
                                           String teacherGroupEmail) throws IOException {
      DropboxInit dropboxInit = dropboxInitRepository.findByCourseIdAndLoginId(courseId, student.getLoginId());
      if (dropboxInit == null) {
         String folderTitlePattern = "{0} Drop Box: {1} ({2})";
         String folderName = MessageFormat.format(toolConfig.getEnvDisplayPrefix() + folderTitlePattern,
               student.getSortableName(), courseTitle, courseId);

         File dropBoxFolder = findFolder(folderName, dropboxFolderId);

         if (dropBoxFolder == null) {
            File gctCourseMetadata = new File();
            gctCourseMetadata.setName(folderName);
            gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
            gctCourseMetadata.setParents(Collections.singletonList(dropboxFolderId));
            gctCourseMetadata.setDescription("Student drop box folder. This folder was created by the Google Course Tools app for Canvas.");
            gctCourseMetadata.setWritersCanShare(false);

//            DropboxFolderCreationCallback callback = DropboxFolderCreationCallback.builder()
//                  .courseId(courseId)
//                  .loginId(student.getLoginId())
//                  .allGroupEmail(allGroupEmail)
//                  .teacherGroupEmail(teacherGroupEmail)
//                  .isLast(isLast)
//                  .gctService(this)
//                  .build();

            dropBoxFolder = driveService.files().create(gctCourseMetadata).setFields("id")
//                  .queue(batch, callback)
                  .execute();
         }

         //Make sure folder id exists now
         if (dropBoxFolder != null && dropBoxFolder.getId() != null) {
            String createdFolderId = dropBoxFolder.getId();
            String loginId = student.getLoginId();
            String studentEmail = loginId + "@iu.edu";

            log.info("Created dropbox folder for " + loginId + " and course " + courseId);
            log.info(teacherGroupEmail);
            log.info(allGroupEmail);

            //Do the permissions
            deleteFolderPermission(createdFolderId, allGroupEmail);

            Permission studentPerm = new Permission();
            studentPerm.setType("user");
            studentPerm.setRole("writer");
            studentPerm.setEmailAddress(studentEmail);
            Permission studPermission = addOrReturnPermission(createdFolderId, studentPerm);

            Permission teacherPerm = new Permission();
            teacherPerm.setType("group");
            teacherPerm.setRole("writer");
            teacherPerm.setEmailAddress(teacherGroupEmail);
            Permission teachPermission = addOrReturnPermission(createdFolderId, teacherPerm);

            //Save the init stuff
            dropboxInit = DropboxInit.builder()
                  .loginId(loginId)
                  .googleLoginId(studentEmail)
                  .courseId(courseId)
                  .folderId(createdFolderId).build();
            dropboxInitRepository.save(dropboxInit);

            return true;
         }
      }
      return false;
   }

   /**
    *
    * @param folderId
    * @param permission
    * @return
    * @throws IOException
    */
   private Permission addOrReturnPermission(String folderId, Permission permission) throws IOException {
      PermissionList permissionList = driveService.permissions().list(folderId).setFields("permissions/id, permissions/emailAddress, permissions/type, permissions/role").execute();

      // find the existing All group from the new folder and purge it
      for (Permission existingPermission: permissionList.getPermissions()) {
         // Group emails are forced to all lowercase, so add a toLowerCase on the permission email to get a match
         if (existingPermission.getEmailAddress().toLowerCase().equals(permission.getEmailAddress()) &&
               existingPermission.getType().equals(permission.getType()) &&
               existingPermission.getRole().equals(permission.getRole())) {
            return existingPermission;
         }
      }
      // If we made it here, need to create it
      Permission createdPermission = driveService.permissions().create(folderId, permission)
            .setSendNotificationEmail(false)
            .execute();
      return createdPermission;
   }

   /**
    * Look for a folder with the given name in the given parent.  Return either the first one found, or null.
    * @param folderName
    * @param parentId
    * @return
    * @throws IOException
    */
   private File findFolder(String folderName, String parentId) throws IOException {
      //Escape the single quotes
      String query = MessageFormat.format("name = ''{0}'' and parents in ''{1}'' and mimeType = ''{2}''",
            folderName, parentId, FOLDER_MIME_TYPE);
      FileList fileList = driveService.files().list().setQ(query).setOrderBy("createdTime").execute();
      if (fileList != null && fileList.getFiles() != null && fileList.getFiles().size() > 0) {
         log.warn("At least one folder returned for this name (" + folderName + ") in the folder with id '" + parentId + "'. Using the earliest one created.");
         return fileList.getFiles().get(0);
      }
      return null;
   }

   /**
    * Get a folder by it's id
    * @param folderId
    * @return
    * @throws IOException
    */
   public File getFolder(String folderId) throws IOException {
      return driveService.files().get(folderId).setFields("id,name,description").execute();
   }

   public File createFileRepositoryFolder(String courseId, String courseTitle, String allGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseId(courseId).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": FILE REPOSITORY";

      File fileRepositoryFolder = findFolder(courseDisplay, courseFolderId);

      if (fileRepositoryFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(courseFolderId));
         gctCourseMetadata.setDescription("Folder for files and folders shared by class members. This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         fileRepositoryFolder = driveService.files().create(gctCourseMetadata)
                 .setEnforceSingleParent(true)
                 .execute();
      }

      log.info("File repository folder: {}", fileRepositoryFolder);

      deleteFolderPermission(fileRepositoryFolder.getId(), allGroupEmail);

      Permission folderPermission = new Permission();
      folderPermission.setType("group");
      folderPermission.setRole("writer");
      folderPermission.setEmailAddress(allGroupEmail);
      Permission permission = driveService.permissions().create(fileRepositoryFolder.getId(), folderPermission)
              .setSendNotificationEmail(false)
              .execute();
      log.info("File repository folder permission: {}", permission);

      return fileRepositoryFolder;
   }

   /**
    * Delete a permission on the given folderId and email
    * @param folderId
    * @param emailToDelete
    * @throws IOException
    */
   private void deleteFolderPermission(String folderId, String emailToDelete) throws IOException {
      PermissionList permissionList = driveService.permissions().list(folderId).setFields("permissions/id, permissions/emailAddress, permissions/type, permissions/role").execute();

      // find the existing All group from the new folder and purge it
      for (Permission existingPermission: permissionList.getPermissions()) {
         // Group emails are forced to all lowercase, so add a toLowerCase on the permission email to get a match
         if (existingPermission.getEmailAddress().toLowerCase().contains(emailToDelete)) {
            driveService.permissions().delete(folderId, existingPermission.getId()).execute();
         }
      }
   }

   /**
    * Verify a legit IU user by the following rules:
    *          confirm that default email is canvas_user_login_id + "@iu.edu"
    *          confirm that SIS ID is 10-digit number beginning with 0 or 2.
    * @param email
    * @param loginId
    * @param sisUserId
    * @return
    */
   public boolean verifyUserEligibility(String email, String loginId, String sisUserId) {
      String calculatedEmail = loginToEmail(loginId);
      boolean validEmail = email != null && loginId != null && email.equalsIgnoreCase(calculatedEmail);
      boolean validSisId = sisUserId != null && (sisUserId.startsWith("0") || sisUserId.startsWith("1"));

      return validEmail || validSisId;
   }

   /**
    * Perform initialization for a user
    * @param courseId
    * @param loginId
    * @param courseInit
    * @param isInstructor
    * @param isTa
    * @param isDesigner
    * @return
    */
   public UserInit userInitialization(String courseId, String loginId, CourseInit courseInit,
                                    boolean isInstructor, boolean isTa, boolean isDesigner) {
      String userEmail = loginToEmail(loginId);

      try {

         Map<Constants.GROUP_TYPES, Group> groups = getGroupsForCourse(courseId);
         String teacherGroupEmail = groups.get(Constants.GROUP_TYPES.TEACHER).getEmail();
         String allGroupEmail = groups.get(Constants.GROUP_TYPES.ALL).getEmail();

         if (isInstructor) {
            Member allMember = addMemberToGroup(allGroupEmail, userEmail, GROUP_ROLES.MANAGER);
            log.info("All Membership details: {}", allMember);

            Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MANAGER);
            log.info("Teacher Membership details: {}", teacherMember);
         } else {
            Member allMember = addMemberToGroup(allGroupEmail, userEmail, GROUP_ROLES.MEMBER);
            log.info("All Membership details: {}", allMember);
            if (isTa) {
               if (courseInit.isTaTeacher()) {
                  Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MEMBER);
                  log.info("Teacher Membership details: {}", teacherMember);
               } else {
                  removeMemberFromGroup(teacherGroupEmail, userEmail);
               }
            } else if (isDesigner) {
               if (courseInit.isDeTeacher()) {
                  Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MEMBER);
                  log.info("Teacher Membership details: {}", teacherMember);
               } else {
                  removeMemberFromGroup(teacherGroupEmail, userEmail);
               }
            }
         }

         UserInit ui = userInitRepository.findByLoginId(loginId);
         if (ui == null) {
            //Create the root folder for the user (if not already created)
            File userRootFolder = createUserRootFolder(userEmail, loginId);
            ui = UserInit.builder()
                  .loginId(loginId)
                  .folderId(userRootFolder.getId())
                  .googleLoginId(userEmail)
                  .build();
            userInitRepository.save(ui);
         }

         //Add a shortcut for the course into the user's folder
         addShortcut(courseInit.getCourseFolderId(), ui.getFolderId());
         return ui;
      } catch (IOException e) {
         log.error("Error with user initialization", e);
      }

      return null;
   }
}
