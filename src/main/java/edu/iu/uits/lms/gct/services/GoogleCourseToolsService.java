package edu.iu.uits.lms.gct.services;

import canvas.client.generated.api.CanvasApi;
import canvas.client.generated.api.ConversationsApi;
import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.GroupsApi;
import canvas.client.generated.api.UsersApi;
import canvas.client.generated.model.ConversationCreateWrapper;
import canvas.client.generated.model.Course;
import canvas.client.generated.model.CourseGroup;
import canvas.client.generated.model.User;
import canvas.helpers.CourseHelper;
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
import edu.iu.uits.lms.gct.Constants.GROUP_ROLES;
import edu.iu.uits.lms.gct.Constants.PERMISSION_ROLES;
import edu.iu.uits.lms.gct.Constants.PERMISSION_TYPE;
import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.model.CourseGroupWrapper;
import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.model.DecoratedCanvasUser;
import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.model.GctProperty;
import edu.iu.uits.lms.gct.model.GroupsInit;
import edu.iu.uits.lms.gct.model.NotificationData;
import edu.iu.uits.lms.gct.model.RosterSyncCourseData;
import edu.iu.uits.lms.gct.model.SerializableGroup;
import edu.iu.uits.lms.gct.model.TokenInfo;
import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.gct.repository.GroupsInitRepository;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import email.client.generated.api.EmailApi;
import email.client.generated.model.EmailDetails;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.gct.Constants.CACHE_DRIVE_SERVICE;
import static edu.iu.uits.lms.gct.Constants.GROUP_NAME_MAX_LENGTH;

@Slf4j
@Service
public class GoogleCourseToolsService implements InitializingBean {

   private static final String APPLICATION_NAME = "LMS Google Course Tools";

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

   @Autowired
   private ToolConfig toolConfig;

   @Autowired
   private CourseInitRepository courseInitRepository;

   @Autowired
   private GroupsInitRepository groupsInitRepository;

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
   private UsersApi usersApi;

   @Autowired
   private GroupsApi groupsApi;

   @Autowired
   private EmailApi emailApi;

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
                  .canvasOrigin(canvasApi.getBaseUrl())
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
    * This strange thing is for "self wiring".  It allows for proxying so that caching will work for calls inside this same service
    */
   @Resource
   private GoogleCourseToolsService self;

   /**
    * Might need to become the end user to do things as/for them
    * If you want to take advantage of caching, you should call this as self.getDriveServiceAsUser
    * @param user Email address for user to impersonate
    * @return
    */
   @Cacheable(value = CACHE_DRIVE_SERVICE)
   public Drive getDriveServiceAsUser(String user) {
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

   /**
    * Check the course title for invalid characters
    * @param courseTitle
    * @return
    */
   public boolean titleHasInvalidCharacters(String courseTitle) {
      char[] invalidCharacters = {'=', '<', '>'};
      return StringUtils.containsAny(courseTitle, invalidCharacters);
   }

   public File createCourseRootFolder(String courseId, String courseTitle, String emailForAccess) throws IOException {
      GctProperty coursesIdProp = gctPropertyRepository.findByKeyAndEnv(PROP_COURSES_FOLDER_KEY, toolConfig.getEnv());
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + MessageFormat.format("{0} ({1})", courseTitle, courseId);

      File courseFolder = findFolder(courseDisplay, coursesIdProp.getValue());

      if (courseFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(coursesIdProp.getValue()));
         gctCourseMetadata.setDescription("Parent folder for shared folders belonging to the course " + courseDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         courseFolder = driveService.files().create(gctCourseMetadata)
               .setEnforceSingleParent(true)
               .execute();
      }

      log.info("User folder: {}", courseFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType(PERMISSION_TYPE.group.name());
      folderPermission.setRole(PERMISSION_ROLES.reader.name());
      folderPermission.setEmailAddress(emailForAccess);
      Permission permission = driveService.permissions().create(courseFolder.getId(), folderPermission)
            .setSendNotificationEmail(false)
            .execute();
      log.info("Folder permission: {}", permission);

      return courseFolder;
   }

   private Permission addOrUpdatePermissionForFile(Drive driveServiceAsUser, String fileId, List<Permission> permissions,
                                                   PERMISSION_TYPE permissionType, String role, String emailForAccess) throws IOException {
      Permission existingPermission = GoogleCourseToolsService.findExistingPerm(permissions, emailForAccess);

      Permission perm = new Permission();
      perm.setType(permissionType.name());
      perm.setRole(role);
      perm.setEmailAddress(emailForAccess);

      Permission permission = null;
      if (existingPermission == null) {
         log.debug("new perm");
         permission = driveServiceAsUser.permissions().create(fileId, perm)
               .setSendNotificationEmail(false)
               .execute();
      } else {
         log.debug("updating perm");
         Permission updPerm = new Permission();
         updPerm.setRole(role);
         permission = driveServiceAsUser.permissions().update(fileId, existingPermission.getId(), updPerm)
               .execute();
      }
      log.info("Permission: {}", permission);
      return permission;
   }

   public File createUserRootFolder(String userEmail, String username) throws IOException {
      GctProperty usersIdProp = gctPropertyRepository.findByKeyAndEnv(PROP_USERS_FOLDER_KEY, toolConfig.getEnv());
      File gctUserMetadata = new File();
      gctUserMetadata.setName(toolConfig.getEnvDisplayPrefix() + "Google Course Tools (" + username + ")");
      gctUserMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
      gctUserMetadata.setParents(Collections.singletonList(usersIdProp.getValue()));
      gctUserMetadata.setDescription("Parent folder for course folders created by the Google Course Tools app for Canvas.  Please do not move or delete.");
      gctUserMetadata.setWritersCanShare(false);

      File userFolder = driveService.files().create(gctUserMetadata)
            .setEnforceSingleParent(true)
            .execute();
      log.info("User folder: {}", userFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType(PERMISSION_TYPE.user.name());
      folderPermission.setRole(PERMISSION_ROLES.writer.name());
      folderPermission.setEmailAddress(userEmail);
      Permission permission = driveService.permissions().create(userFolder.getId(), folderPermission)
            .setSendNotificationEmail(false)
            .execute();
      log.info("Folder permission: {}", permission);

      //Create the shortcut to the user's root
      addShortcut(userFolder, null, userEmail);
      return userFolder;
   }

   public List<String> initBaseFolders() throws IOException {
      List<String> ids = new ArrayList<>();

      //Check for the existence of the required root folders.
      GctProperty rootIdProp = gctPropertyRepository.findByKeyAndEnv(PROP_ROOT_FOLDER_KEY, toolConfig.getEnv());
      GctProperty coursesIdProp = gctPropertyRepository.findByKeyAndEnv(PROP_COURSES_FOLDER_KEY, toolConfig.getEnv());
      GctProperty usersIdProp = gctPropertyRepository.findByKeyAndEnv(PROP_USERS_FOLDER_KEY, toolConfig.getEnv());

      if (rootIdProp == null) {
         File rootFolderMetadata = new File();
         rootFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "Google Course Tools Admin");
         rootFolderMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
//      rootFolderMetadata.setShared(false);
         rootFolderMetadata.setDescription("Container folder for Google Course Tools Assets.");
         rootFolderMetadata.setWritersCanShare(false);
         File rootFolder = driveService.files().create(rootFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, shared, description, writersCanShare")
               .execute();
         log.info("Root folder info: {}", rootFolder);
         rootIdProp = new GctProperty(PROP_ROOT_FOLDER_KEY, rootFolder.getId(), toolConfig.getEnv());
         gctPropertyRepository.save(rootIdProp);
         ids.add(rootFolder.getId());
      }

      if (coursesIdProp == null) {
         File coursesFolderMetadata = new File();
         coursesFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "GCT Courses");
         coursesFolderMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
//      coursesFolderMetadata.setShared(false);
         coursesFolderMetadata.setDescription("Container for course folders created by the Google Course Tools LTI.");
         coursesFolderMetadata.setWritersCanShare(false);
         coursesFolderMetadata.setParents(Collections.singletonList(rootIdProp.getValue()));
         File coursesFolder = driveService.files().create(coursesFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, parents")
               .execute();
         log.info("Course folder info: {}", coursesFolder);
         coursesIdProp = new GctProperty(PROP_COURSES_FOLDER_KEY, coursesFolder.getId(), toolConfig.getEnv());
         gctPropertyRepository.save(coursesIdProp);
         ids.add(coursesFolder.getId());
      }

      if (usersIdProp == null) {
         File usersFolderMetadata = new File();
         usersFolderMetadata.setName(toolConfig.getEnvDisplayPrefix() + "GCT User Folders");
         usersFolderMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
         usersFolderMetadata.setParents(Collections.singletonList(rootIdProp.getValue()));
//      usersFolderMetadata.setShared(false);
         usersFolderMetadata.setDescription("Container for user folders created by the Google Course Tools LTI.");
         usersFolderMetadata.setWritersCanShare(false);
         File usersFolder = driveService.files().create(usersFolderMetadata)
               .setEnforceSingleParent(true)
//            .setFields("id, parents")
               .execute();
         log.info("Users folder info: {}", usersFolder);
         usersIdProp = new GctProperty(PROP_USERS_FOLDER_KEY, usersFolder.getId(), toolConfig.getEnv());
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
    * We notice seemingly random failures that could be timing/cluster related with the google apis.
    * Even though we've created the group, it's not available when adding the member below.
    * So, we're introducing some retry logic.
    * @param groupEmail
    * @param userEmail
    * @param role
    * @return
    * @throws IOException
    */
   private Member addMemberToGroupWithRetry(String groupEmail, String userEmail, GROUP_ROLES role) throws IOException {
      RetryTemplate retry = new RetryTemplate();
      retry.setRetryPolicy(new SimpleRetryPolicy(3, Collections.singletonMap(IOException.class, true)));
      retry.setBackOffPolicy(new ExponentialBackOffPolicy());

      return retry.execute(retryContext -> {
         log.debug("Adding member {} to group {} as {} attempt #{}", userEmail, groupEmail, role.name(), retryContext.getRetryCount()+1);
         return addMemberToGroup(groupEmail, userEmail, role);
      });
   }

   /**
    * Google API has a limit of 73 characters for the group name.  Make sure our group name will not be longer than that!
    * If it is, truncate the course name until we're good.
    * @param canvasCourseId
    * @param courseName
    * @param groupNamePattern
    * @return
    */
   protected String buildValidatedGroupName(String canvasCourseId, String courseName, String groupNamePattern) {
      String groupName = MessageFormat.format(groupNamePattern, toolConfig.getEnvDisplayPrefix(), courseName, canvasCourseId);

      int groupNameLength = groupName.length();
      if (groupNameLength > GROUP_NAME_MAX_LENGTH) {
         int diff = groupNameLength - GROUP_NAME_MAX_LENGTH;
         String truncatedCourseName = courseName.substring(0, courseName.length() - diff);
         groupName = MessageFormat.format(groupNamePattern, toolConfig.getEnvDisplayPrefix(), truncatedCourseName, canvasCourseId);
      }
      return groupName;
   }

   /**
    * Google API has a limit of 73 characters for the group name.  Make sure our google group name will not be longer than that!
    * If it is, truncate the canvas group name until we're good.
    * @param courseGroup
    * @param groupNamePattern
    * @return
    */
   protected String buildValidatedGroupName(CourseGroup courseGroup, String groupNamePattern) {
      String groupName = MessageFormat.format(groupNamePattern, toolConfig.getEnvDisplayPrefix(), courseGroup.getName(), courseGroup.getCourseId(), courseGroup.getId());

      int groupNameLength = groupName.length();
      if (groupNameLength > GROUP_NAME_MAX_LENGTH) {
         int diff = groupNameLength - GROUP_NAME_MAX_LENGTH;
         String truncatedCourseName = courseGroup.getName().substring(0, courseGroup.getName().length() - diff);
         groupName = MessageFormat.format(groupNamePattern, toolConfig.getEnvDisplayPrefix(), truncatedCourseName, courseGroup.getCourseId(), courseGroup.getId());
      }
      return groupName;
   }

   /**
    * Create a group, if it does not already exist
    * @param email
    * @param groupName
    * @param groupDescription
    * @return
    * @throws IOException
    */
   private Group createGroup(String email, String groupName, String groupDescription) throws IOException {
      Group group;
      try {
         //Look for an existing group
         group = getGroup(email);
         log.debug("Returning existing group: {}", email);
      } catch (IOException e) {
         //Group doesn't exist.  Create it.
         Group newGroup = new Group();
         newGroup.setName(groupName);
         newGroup.setDescription(groupDescription);
         newGroup.setEmail(email);

         group = directoryService.groups().insert(newGroup).execute();
         log.debug("Created new group: {}", email);
      }
      return group;
   }

   /**
    * Used to escape any query strings for searching google artifacts.  So far, only need to escape single quotes
    * @param input
    * @return
    */
   protected String escapeQueryValues(String input) {
      return input.replace("'", "\\'");
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
      String groupNamePattern = "{0}{1}-{2} All";
      String groupName = buildValidatedGroupName(canvasCourseId, courseName, groupNamePattern);
      String groupDescription = "Google group for all members of " + courseName + "-" + canvasCourseId;

      Group group = createGroup(email, groupName, groupDescription);

      // this is a default in all groups created by our tool
      addMemberToGroupWithRetry(email, toolConfig.getImpersonationAccount(), GROUP_ROLES.OWNER);

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
         groupSettings.setWhoCanPostMessage("ALL_MANAGERS_CAN_POST");
         groupSettings.setAllowWebPosting("false");
         groupSettings.setIsArchived("false");
      }
      groupsSettingsService.groups().update(group.getEmail(), groupSettings).execute();

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
      String groupNamePattern = "{0}{1}-{2} Teachers";
      String groupName = buildValidatedGroupName(canvasCourseId, courseName, groupNamePattern);
      String groupDescription = "Google group for instructors of " + courseName + "-" + canvasCourseId;

      Group group = createGroup(email, groupName, groupDescription);

      // this is a default in all groups created by our tool
      addMemberToGroupWithRetry(email, toolConfig.getImpersonationAccount(), GROUP_ROLES.OWNER);

      com.google.api.services.groupssettings.model.Groups groupSettings = groupsSettingsService.groups().get(group.getEmail()).execute();
      //TODO Any chance there are constants for these somewhere?
      groupSettings.setWhoCanJoin("INVITED_CAN_JOIN");
      groupSettings.setWhoCanViewMembership("ALL_MANAGERS_CAN_VIEW");
      groupSettings.setWhoCanViewGroup("ALL_MEMBERS_CAN_VIEW");
      groupSettings.setAllowExternalMembers("true");
      groupSettings.setWhoCanPostMessage("ALL_MANAGERS_CAN_POST");
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

      return group;
   }

   /**
    *
    * @param courseGroup
    * @return
    * @throws IOException
    */
   private Group createGroupForCanvasGroup(CourseGroup courseGroup) throws IOException {
      String email = getEmailForCourseGroup(courseGroup);

      String groupNamePattern = "{0}{1}-{2}-{3}";
      String groupName = buildValidatedGroupName(courseGroup, groupNamePattern);
      String groupDescription = "Google group for members of " + courseGroup.getName();

      Group group = createGroup(email, groupName, groupDescription);

      // this is a default in all groups created by our tool
      addMemberToGroupWithRetry(email, toolConfig.getImpersonationAccount(), GROUP_ROLES.OWNER);

      com.google.api.services.groupssettings.model.Groups groupSettings = groupsSettingsService.groups().get(group.getEmail()).execute();
      //TODO Any chance there are constants for these somewhere?
      groupSettings.setWhoCanJoin("INVITED_CAN_JOIN");
      groupSettings.setWhoCanViewMembership("ALL_MANAGERS_CAN_VIEW");
      groupSettings.setWhoCanViewGroup("ALL_MEMBERS_CAN_VIEW");
      groupSettings.setAllowExternalMembers("true");
      groupSettings.setWhoCanPostMessage("ALL_MANAGERS_CAN_POST");
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

   public CourseGroupWrapper getGroupsForCourse(String courseId) throws IOException {
      Groups groups = directoryService.groups().list()
            .setQuery("email:" + toolConfig.getEnvDisplayPrefix() + courseId + "-*")
            .setDomain(toolConfig.getDomain())
            .execute();

      CourseGroupWrapper cgw = new CourseGroupWrapper();

      for (Group group : groups.getGroups()) {
         if (group.getEmail().contains("all")) {
            cgw.setAllGroup(new SerializableGroup(group));
         } else if (group.getEmail().contains("teachers")) {
            cgw.setTeacherGroup(new SerializableGroup(group));
         } else {
            cgw.addCanvasGroup(new SerializableGroup(group));
         }
      }
      return cgw;
   }

   public List<com.google.api.services.admin.directory.model.User> getUsers() throws IOException {
      Users users = directoryService.users().list()
            .setDomain(toolConfig.getDomain())
            .execute();
      return users.getUsers();
   }

   public List<Member> addMembersToGroup(String groupEmail, List<String> userEmails, GROUP_ROLES role) throws IOException {
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
      List<Member> allMembers = new ArrayList<>();
      if (members != null) {
         allMembers.addAll(members.getMembers());
         //Page through the data
         while (members.getNextPageToken() != null) {
            members = directoryService.members().list(groupEmail).setPageToken(members.getNextPageToken())
                  .execute();
            allMembers.addAll(members.getMembers());
         }
      }
      return allMembers;
   }

   /**
    * Is the user a member of this group
    * @param groupEmail
    * @param userLoginId
    * @return
    */
   public boolean isUserInGroup(String groupEmail, String userLoginId) {
      try {
         String userEmail = loginToEmail(userLoginId);
         Member member = directoryService.members().get(groupEmail, userEmail).execute();
         if (member != null) {
            return true;
         }
      } catch (IOException io) {
         return false;
      }
      return false;
   }

   /**
    * Get the folderId that goes along with a canvas course group
    * @param canvasCourseId
    * @return A map where the key is the email for the course group and the value is the folder id for the Google folder
    */
   public Map<String, String> getFolderIdByCourseGroup(String canvasCourseId) {
      List<GroupsInit> allCourseGroups = groupsInitRepository.findByCanvasCourseIdAndEnv(canvasCourseId, toolConfig.getEnv());
      return allCourseGroups.stream()
            .collect(Collectors.toMap(gi -> getEmailForCourseGroup(gi.getCanvasCourseId(), gi.getCanvasGroupId()).toLowerCase(), GroupsInit::getFolderId));
   }

   /**
    * Get the GroupsInit object associated with this canvasCourseId and folderId
    * @param canvasCourseId
    * @param folderId
    * @return
    */
   public GroupsInit getGroupsInit(String canvasCourseId, String folderId) {
      return groupsInitRepository.findByCanvasCourseIdAndFolderIdAndEnv(canvasCourseId, folderId, toolConfig.getEnv());
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
      return courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv());
   }

   public DropboxInit getDropboxInit(String courseId, String loginId) {
      return dropboxInitRepository.findByCourseIdAndLoginIdAndEnv(courseId, loginId, toolConfig.getEnv());
   }

   public DropboxInit getDropboxInitByGoogleLogin(String courseId, String googleLoginId) {
      return dropboxInitRepository.findByCourseIdAndGoogleLoginIdAndEnv(courseId, googleLoginId, toolConfig.getEnv());
   }

   public UserInit getUserInit(String loginId) {
      return userInitRepository.findByLoginIdAndEnv(loginId, toolConfig.getEnv());
   }

   /**
    * Do initialization for a course.
    * @param courseId
    * @param courseTitle
    * @param courseSisId
    * @param courseCode
    * @param mailingListActive
    * @return
    */
   public CourseInit courseInitialization(String courseId, String courseTitle, String courseSisId, String courseCode, boolean mailingListActive) throws IOException {
      CourseInit ci = new CourseInit();

      //Create the course groups
      Map<Constants.GROUP_TYPES, Group> groups = createCourseGroups(courseId, courseTitle, mailingListActive);
      log.info("Group details: {}", groups);

      //Create the root folder for this course
      File courseRootFolder = createCourseRootFolder(courseId, courseTitle, groups.get(Constants.GROUP_TYPES.ALL).getEmail());
      log.info("Course root folder: {}", courseRootFolder);

      ci.setCourseId(courseId);
      ci.setCourseFolderId(courseRootFolder.getId());
      ci.setEnv(toolConfig.getEnv());
      ci.setSisCourseId(courseSisId);
      ci.setCourseCode(courseCode);

      courseInitRepository.save(ci);
      return ci;
   }

   /**
    *
    * Create (or find existing) shortcut to target file/folder inside a given parent folder
    * @param targetId Target file id that you want to create a shortcut to
    * @param parent Parent folder id where you want to put the shortcut
    * @param asUserEmail User's email address to impersonate as they are the file owner
    * @throws IOException
    */
   public void addShortcut(String targetId, String parent, String asUserEmail) throws IOException {
      Drive localDriveService = driveService;
      if (asUserEmail != null) {
         localDriveService = self.getDriveServiceAsUser(asUserEmail);
      }
      File folder = localDriveService.files().get(targetId).execute();
      addShortcut(folder, parent, asUserEmail);
   }

   /**
    * Create (or find existing) shortcut to target file/folder inside a given parent folder
    * @param target Target file that you want to create a shortcut to
    * @param parent Parent folder id where you want to put the shortcut
    * @param asUserEmail User's email address to impersonate as they are the file owner
    * @throws IOException
    */
   private void addShortcut(File target, String parent, String asUserEmail) throws IOException {
      File shortcut = findShortcutForTarget(target.getName(), target.getId(), parent, asUserEmail);

      if (shortcut == null) {
         //Create the shortcut
         File newShortcut = new File();
         newShortcut.setName(target.getName());
         newShortcut.setMimeType(Constants.SHORTCUT_MIME_TYPE);
         if (parent != null) {
            newShortcut.setParents(Collections.singletonList(parent));
         }
         File.ShortcutDetails sd = new File.ShortcutDetails();
         sd.setTargetId(target.getId());
         sd.setTargetMimeType(target.getMimeType());
         newShortcut.setShortcutDetails(sd);

         Drive localDriveService = driveService;
         if (asUserEmail != null) {
            localDriveService = self.getDriveServiceAsUser(asUserEmail);
         }

         shortcut = localDriveService.files().create(newShortcut)
               .execute();
      }
      log.info("Shortcut Info: {}", shortcut);
   }

   /**
    * Set the given permissions on the given file and create a shortcut in the given destination
    * @param fileId File to act on
    * @param destFolderId Destination folder where the shortcut will be created
    * @param groupsForCourse Groups for the course
    * @param allPerm Permission to set for the "all" group
    * @param teacherPerm Permission to set for the "teacher" group
    * @param courseGroupPerm Permission to set for the canvas course group (as specified by emailForCourseGroup)
    * @param asUser Login id of the user we are acting as
    * @param emailForCourseGroup Email identifier for the canvas course group
    * @throws IOException
    */
   public void shareAndAddShortcut(String fileId, String destFolderId, CourseGroupWrapper groupsForCourse,
                                   String allPerm, String teacherPerm, String courseGroupPerm, String asUser,
                                   String emailForCourseGroup) throws IOException {
      String asUserEmail = loginToEmail(asUser);
      Drive driveServiceAsUser = self.getDriveServiceAsUser(asUserEmail);
      File fileChanges = new File();
      fileChanges.setWritersCanShare(false);
      File file = driveServiceAsUser.files().update(fileId, fileChanges)
            .setFields("id,name,permissions")
            .execute();

      log.debug("File: {}", file);

      //share (if set)
      if (allPerm != null) {
         addOrUpdatePermissionForFile(driveServiceAsUser, fileId, file.getPermissions(),
               PERMISSION_TYPE.group,
               allPerm,
               groupsForCourse.getAllGroup().getEmail());
      }

      //share (if set)
      if (courseGroupPerm != null) {
         addOrUpdatePermissionForFile(driveServiceAsUser, fileId, file.getPermissions(),
               PERMISSION_TYPE.group,
               courseGroupPerm,
               emailForCourseGroup);
      }

      //share
      addOrUpdatePermissionForFile(driveServiceAsUser, fileId, file.getPermissions(),
            PERMISSION_TYPE.group,
            teacherPerm,
            groupsForCourse.getTeacherGroup().getEmail());

      //shortcut
      addShortcut(file, destFolderId, asUserEmail);
   }

   /**
    * Find an existing shortcut for a target folder/file
    * @param itemName Name of the shortcut to look up
    * @param targetFileId Target folder/file id
    * @param parentFolderId Id of the parent folder where the shortcut would live
    * @param asUserEmail Email address that owns the content (null if it's our service account)
    * @return The shortcut, or null if none found
    */
   private File findShortcutForTarget(String itemName, String targetFileId, String parentFolderId, String asUserEmail) {
      Drive localDriveService = driveService;
      if (asUserEmail != null) {
         localDriveService = self.getDriveServiceAsUser(asUserEmail);
      }

      //Escape the single quotes
      String query = MessageFormat.format("name = ''{0}'' and parents in ''{1}'' and mimeType = ''{2}''",
            escapeQueryValues(itemName), parentFolderId, Constants.SHORTCUT_MIME_TYPE);

      File shortcut = null;
      try {
         FileList fileList = localDriveService.files().list()
               .setQ(query)
               .setOrderBy("createdTime")
               .setFields("files/shortcutDetails,files/id")
               .execute();
         shortcut = fileList.getFiles().stream()
               .filter(f -> targetFileId.equals(f.getShortcutDetails().getTargetId()))
               .findFirst().orElse(null);
      } catch (IOException e) {
         log.warn("No shortcut found.  Returning null.");
      }

      return shortcut;
   }

   public void saveCourseInit(CourseInit courseInit) {
      courseInitRepository.save(courseInit);
   }

   public File createCourseFileFolder(String courseId, String courseTitle, String teacherGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv()).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": COURSE FILES";

      File courseFileFolder = findFolder(courseDisplay, courseFolderId);

      if (courseFileFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
         gctCourseMetadata.setParents(Collections.singletonList(courseFolderId));
         gctCourseMetadata.setDescription("Folder for sharing files with members of " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         gctCourseMetadata.setWritersCanShare(false);

         courseFileFolder = driveService.files().create(gctCourseMetadata)
                 .setEnforceSingleParent(true)
                 .execute();
      }

      log.info("Course files folder: {}", courseFileFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType(PERMISSION_TYPE.group.name());
      folderPermission.setRole(PERMISSION_ROLES.writer.name());
      folderPermission.setEmailAddress(teacherGroupEmail);
      Permission permission = driveService.permissions().create(courseFileFolder.getId(), folderPermission)
              .setSendNotificationEmail(false)
              .execute();
      log.info("Course files folder permission: {}", permission);

      return courseFileFolder;
   }

   /**
    *
    * @param permissions
    * @param groupEmail
    * @return
    */
   public static String getExistingRoleForGroupPerm(List<Permission> permissions, String groupEmail, String defaultPerm) {
      Permission perm = findExistingPerm(permissions, groupEmail);
      if (perm != null) {
         return perm.getRole();
      }
      return defaultPerm;
   }

   private static Permission findExistingPerm(List<Permission> permissions, String groupEmail) {
      Permission perm = CollectionUtils.emptyIfNull(permissions).stream()
            .filter(p -> PERMISSION_TYPE.group.name().equals(p.getType()) && groupEmail.equalsIgnoreCase(p.getEmailAddress()))
            .findFirst().orElse(null);
      return perm;
   }

   public File createInstructorFileFolder(String courseId, String courseTitle, String allGroupEmail, String teacherGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv()).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": INSTRUCTOR FILES";

      File instructorFileFolder = findFolder(courseDisplay, courseFolderId);

      if (instructorFileFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
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
      folderPermission.setType(PERMISSION_TYPE.group.name());
      folderPermission.setRole(PERMISSION_ROLES.writer.name());
      folderPermission.setEmailAddress(teacherGroupEmail);
      Permission permission = driveService.permissions().create(instructorFileFolder.getId(), folderPermission)
              .setSendNotificationEmail(false)
              .execute();
      log.info("Instructor files folder permission: {}", permission);

      return instructorFileFolder;
   }

   public File createCanvasGroupsFolder(String courseId, String courseTitle, String allGroupEmail, String teacherGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv()).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String folderDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": GROUP FILES";

      File groupsFolder = findFolder(folderDisplay, courseFolderId);

      if (groupsFolder == null) {
         File metadata = new File();
         metadata.setName(folderDisplay);
         metadata.setMimeType(Constants.FOLDER_MIME_TYPE);
         metadata.setParents(Collections.singletonList(courseFolderId));
         metadata.setDescription("Parent folder for Canvas Group folders " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
         metadata.setWritersCanShare(false);

         groupsFolder = driveService.files().create(metadata)
               .setEnforceSingleParent(true)
               .execute();
      }

      log.info("Groups files folder: {}", groupsFolder);

      Permission folderPermission = new Permission();
      folderPermission.setType(PERMISSION_TYPE.group.name());
      folderPermission.setRole(PERMISSION_ROLES.writer.name());
      folderPermission.setEmailAddress(teacherGroupEmail);
      Permission permission = driveService.permissions().create(groupsFolder.getId(), folderPermission)
            .setSendNotificationEmail(false)
            .execute();
      log.info("Groups files folder permission: {}", permission);

      List<CourseGroup> courseGroups = getCanvasGroupsForCourse(courseId);
      createCanvasGroupFolders(groupsFolder.getId(), courseId, allGroupEmail, teacherGroupEmail, courseGroups);

      return groupsFolder;
   }

   public void createCanvasGroupFolders(String parentFolderId, String courseId, String allGroupEmail, String teacherGroupEmail, List<CourseGroup> courseGroups) throws IOException {
      for (CourseGroup cg : courseGroups) {
         Group group = createGroupForCanvasGroup(cg);
         List<User> groupUsers = groupsApi.getUsersInGroup(cg.getId(), true);
         List<String> userEmails = groupUsers.stream().map(User::getLoginId)
               .map(this::loginToEmail)
               .collect(Collectors.toList());
         addMembersToGroup(getEmailForCourseGroup(cg), userEmails, GROUP_ROLES.MEMBER);

         List<Member> teacherMembers = getMembersOfGroup(teacherGroupEmail);
         List<String> teacherEmails = teacherMembers.stream().map(Member::getEmail).collect(Collectors.toList());
         addMembersToGroup(getEmailForCourseGroup(cg), teacherEmails, GROUP_ROLES.MANAGER);
         createCanvasGroupFolder(parentFolderId, cg, allGroupEmail);
      }
   }

   /**
    *
    * @param courseGroup
    * @return
    */
   public String getEmailForCourseGroup(CourseGroup courseGroup) {
      return getEmailForCourseGroup(courseGroup.getCourseId(), courseGroup.getId());
   }

   /**
    *
    * @param courseId
    * @param groupId
    * @return
    */
   public String getEmailForCourseGroup(String courseId, String groupId) {
      return MessageFormat.format("{0}{1}-{2}-iu-group@iu.edu", toolConfig.getEnvDisplayPrefix(), courseId, groupId);
   }

   /**
    *
    * @param parentFolderId
    * @param courseGroup
    * @param allGroupEmail
    * @throws IOException
    */
   private void createCanvasGroupFolder(String parentFolderId, CourseGroup courseGroup, String allGroupEmail) throws IOException {
      String courseParentDisplay = MessageFormat.format("{0}-{1}-{2}", courseGroup.getName(), courseGroup.getCourseId(), courseGroup.getId());
      String folderDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + " Files";

      GroupsInit groupsInit = groupsInitRepository.findByCanvasCourseIdAndCanvasGroupIdAndEnv(courseGroup.getCourseId(), courseGroup.getId(), toolConfig.getEnv());

      if (groupsInit == null) {
         File groupFolder = findFolder(folderDisplay, parentFolderId);

         if (groupFolder == null) {
            File metadata = new File();
            metadata.setName(folderDisplay);
            metadata.setMimeType(Constants.FOLDER_MIME_TYPE);
            metadata.setParents(Collections.singletonList(parentFolderId));
            metadata.setDescription("Folder for members of Canvas group " + courseParentDisplay + ". This folder was created by the Google Course Tools app for Canvas.");
            metadata.setWritersCanShare(false);

            groupFolder = driveService.files().create(metadata)
                  .setEnforceSingleParent(true)
                  .execute();
         }

         log.info("Group files folder: {}", groupFolder);

         deleteFolderPermission(groupFolder.getId(), allGroupEmail);

         Permission folderPermission = new Permission();
         folderPermission.setType(PERMISSION_TYPE.group.name());
         folderPermission.setRole(PERMISSION_ROLES.writer.name());
         folderPermission.setEmailAddress(getEmailForCourseGroup(courseGroup));
         Permission permission = driveService.permissions().create(groupFolder.getId(), folderPermission)
               .setSendNotificationEmail(false)
               .execute();
         log.info("Group files folder permission: {}", permission);

         groupsInit = GroupsInit.builder().canvasCourseId(courseGroup.getCourseId())
               .canvasGroupId(courseGroup.getId())
               .folderId(groupFolder.getId())
               .env(toolConfig.getEnv()).build();

         groupsInitRepository.save(groupsInit);
      }
   }

   public File createDropboxFolder(String courseId, String courseTitle) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv()).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": DROP BOXES";

      File dropBoxFolder = findFolder(courseDisplay, courseFolderId);

      if (dropBoxFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
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
         DropboxInit dropboxInit = dropboxInitRepository.findByCourseIdAndLoginIdAndEnv(courseId, student.getLoginId(), toolConfig.getEnv());
         createStudentDropboxFolder(courseId, courseTitle, dropboxFolderId, student, allGroupEmail, teacherGroupEmail, dropboxInit); //, batch) || batchNotEmpty;
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

   public DropboxInit createStudentDropboxFolder(String courseId, String courseTitle, String dropboxFolderId, String userLoginId,
                                                 String allGroupEmail, String teacherGroupEmail, DropboxInit dropboxInit) throws IOException {
      User user = usersApi.getUserBySisLoginId(userLoginId);

      boolean created = createStudentDropboxFolder(courseId, courseTitle, dropboxFolderId, user, allGroupEmail, teacherGroupEmail, dropboxInit);
      return dropboxInit;
   }

   private boolean createStudentDropboxFolder(String courseId, String courseTitle, String dropboxFolderId,
                                           User student, String allGroupEmail, String teacherGroupEmail, DropboxInit dropboxInit) throws IOException {
      boolean userEligible = verifyUserEligibility(student.getEmail(), student.getLoginId(), student.getSisUserId());
      if (dropboxInit == null && userEligible) {
         String folderTitlePattern = "{0} ({1}): {2} ({3})";
         String folderName = MessageFormat.format(toolConfig.getEnvDisplayPrefix() + folderTitlePattern,
               student.getSortableName(), student.getLoginId(), courseTitle, courseId);

         File dropBoxFolder = findFolder(folderName, dropboxFolderId);

         if (dropBoxFolder == null) {
            File gctCourseMetadata = new File();
            gctCourseMetadata.setName(folderName);
            gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
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
            studentPerm.setType(PERMISSION_TYPE.user.name());
            studentPerm.setRole(PERMISSION_ROLES.writer.name());
            studentPerm.setEmailAddress(studentEmail);
            Permission studPermission = addOrReturnPermission(createdFolderId, studentPerm);

            Permission teacherPerm = new Permission();
            teacherPerm.setType(PERMISSION_TYPE.group.name());
            teacherPerm.setRole(PERMISSION_ROLES.writer.name());
            teacherPerm.setEmailAddress(teacherGroupEmail);
            Permission teachPermission = addOrReturnPermission(createdFolderId, teacherPerm);

            //Save the init stuff
            dropboxInit = DropboxInit.builder()
                  .loginId(loginId)
                  .googleLoginId(studentEmail)
                  .courseId(courseId)
                  .folderId(createdFolderId)
                  .env(toolConfig.getEnv()).build();
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
            escapeQueryValues(folderName), parentId, Constants.FOLDER_MIME_TYPE);
      FileList fileList = driveService.files().list().setQ(query).setOrderBy("createdTime").execute();
      if (fileList != null && fileList.getFiles() != null && fileList.getFiles().size() > 0) {
         log.warn("At least one folder returned for this name (" + folderName + ") in the folder with id '" + parentId + "'. Using the earliest one created.");
         return fileList.getFiles().get(0);
      }
      return null;
   }

   public static boolean isFolder(File file) {
      return Constants.FOLDER_MIME_TYPE.equals(file.getMimeType());
   }

   /**
    * Get a folder by it's id
    * @param folderId Id of folder
    * @return
    * @throws IOException
    */
   public File getFolder(String folderId) throws IOException {
      return driveService.files().get(folderId).setFields("id,name,description,webViewLink").execute();
   }

   /**
    * Delete a folder by id
    * @param folderId Id of folder
    * @throws IOException
    */
   private void deleteFolder(String folderId) throws IOException {
      driveService.files().delete(folderId).execute();
   }

   public List<File> getFiles(String[] fileIds, String asUser) throws IOException {
      String userEmail = loginToEmail(asUser);
      List<File> files = new ArrayList<>();
      Drive localDriveService = driveService;
      if (asUser != null) {
         localDriveService = self.getDriveServiceAsUser(userEmail);
      }
      for (String fileId: fileIds) {
         File file = localDriveService.files().get(fileId).setFields("id,name,kind,mimeType,permissions,iconLink").execute();
         files.add(file);
      }
      return files;
   }

   public File createFileRepositoryFolder(String courseId, String courseTitle, String allGroupEmail) throws IOException {
      String courseFolderId = courseInitRepository.findByCourseIdAndEnv(courseId, toolConfig.getEnv()).getCourseFolderId();
      String courseParentDisplay = MessageFormat.format("{0} ({1})", courseTitle, courseId);
      String courseDisplay = toolConfig.getEnvDisplayPrefix() + courseParentDisplay + ": FILE REPOSITORY";

      File fileRepositoryFolder = findFolder(courseDisplay, courseFolderId);

      if (fileRepositoryFolder == null) {
         File gctCourseMetadata = new File();
         gctCourseMetadata.setName(courseDisplay);
         gctCourseMetadata.setMimeType(Constants.FOLDER_MIME_TYPE);
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
      folderPermission.setType(PERMISSION_TYPE.group.name());
      folderPermission.setRole(PERMISSION_ROLES.writer.name());
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
      boolean validSisId = sisUserId != null && sisUserId.length() == 10 &&
            (sisUserId.startsWith("0") || sisUserId.startsWith("2"));

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

         CourseGroupWrapper groups = getGroupsForCourse(courseId);
         String teacherGroupEmail = groups.getTeacherGroup().getEmail();
         String allGroupEmail = groups.getAllGroup().getEmail();

         //Get canvas group memberships
         List<CourseGroup> canvasCourseGroups = getCanvasGroupsForCourse(courseId);

         boolean addToCanvasGroupAsManager = false;
         boolean removeFromCanvasGroupAsManager = false;

         if (isInstructor) {
            Member allMember = addMemberToGroup(allGroupEmail, userEmail, GROUP_ROLES.MANAGER);
            log.info("All Membership details: {}", allMember);

            Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MANAGER);
            log.info("Teacher Membership details: {}", teacherMember);

            addToCanvasGroupAsManager = true;
         } else {
            Member allMember = addMemberToGroup(allGroupEmail, userEmail, GROUP_ROLES.MEMBER);
            log.info("All Membership details: {}", allMember);
            if (isTa) {
               if (courseInit.isTaTeacher()) {
                  Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MEMBER);
                  log.info("Teacher Membership details: {}", teacherMember);
                  addToCanvasGroupAsManager = true;
               } else {
                  removeMemberFromGroup(teacherGroupEmail, userEmail);
                  removeFromCanvasGroupAsManager = true;
               }
            } else if (isDesigner) {
               if (courseInit.isDeTeacher()) {
                  Member teacherMember = addMemberToGroup(teacherGroupEmail, userEmail, GROUP_ROLES.MEMBER);
                  log.info("Teacher Membership details: {}", teacherMember);
                  addToCanvasGroupAsManager = true;
               } else {
                  removeMemberFromGroup(teacherGroupEmail, userEmail);
                  removeFromCanvasGroupAsManager = true;
               }
            }
         }

         //Sync the user to the groups
         syncCanvasAndGoogleGroups(canvasCourseGroups, loginId);

         for (CourseGroup canvasGroup : canvasCourseGroups) {
            String groupEmail = getEmailForCourseGroup(canvasGroup);
            if (addToCanvasGroupAsManager) {
               Member member = addMemberToGroup(groupEmail, userEmail, GROUP_ROLES.MANAGER);
               log.info("Canvas Group {} Membership details: {}", groupEmail, member);
            }
            if (removeFromCanvasGroupAsManager) {
               removeMemberFromGroup(groupEmail, userEmail);
            }
         }

         //Make sure group folders exist
         if (courseInit.getGroupsFolderId() != null) {
            createCanvasGroupFolders(courseInit.getGroupsFolderId(), courseId, allGroupEmail, teacherGroupEmail, canvasCourseGroups);
         }

         UserInit ui = userInitRepository.findByLoginIdAndEnv(loginId, toolConfig.getEnv());
         if (ui == null) {
            //Create the root folder for the user (if not already created)
            File userRootFolder = createUserRootFolder(userEmail, loginId);
            ui = UserInit.builder()
                  .loginId(loginId)
                  .folderId(userRootFolder.getId())
                  .googleLoginId(userEmail)
                  .env(toolConfig.getEnv())
                  .build();
            userInitRepository.save(ui);
         }

         //Add a shortcut for the course into the user's folder
         addShortcut(courseInit.getCourseFolderId(), ui.getFolderId(), null);
         return ui;
      } catch (IOException e) {
         log.error("Error with user initialization", e);
      }

      return null;
   }

   /**
    * Perform a roster sync for all active courses
    */
   public void rosterSyncBatch() {
      List<String> successes = new ArrayList<>();
      List<String> errors = new ArrayList<>();
      List<String> inactivated = new ArrayList<>();

      List<CourseInit> courses = courseInitRepository.findBySyncStatusAndEnv(CourseInit.SYNC_STATUS.ACTIVE, toolConfig.getEnv());
      for (CourseInit courseInit : courses) {
         String courseId = courseInit.getCourseId();
         Course course = coursesApi.getCourse(courseId);
         if (course != null && course.getCourseCode() != null && course.getCourseCode().equals(courseInit.getCourseCode())) {
            String courseDisplay = MessageFormat.format("{0} ({1})", course.getName(), courseId);
            try {
               CourseGroupWrapper groups = getGroupsForCourse(courseId);
               String allGroupEmail = groups.getAllGroup().getEmail();
               String teacherGroupEmail = groups.getTeacherGroup().getEmail();


               RosterSyncCourseData data = new RosterSyncCourseData(courseId, course.getName(), allGroupEmail, teacherGroupEmail);
               RetryTemplate retry = new RetryTemplate();
               retry.setRetryPolicy(new SimpleRetryPolicy(3, Collections.singletonMap(IOException.class, true)));
               retry.setBackOffPolicy(new ExponentialBackOffPolicy());

               retry.execute(retryContext -> {
                  log.debug("Roster Sync for {} #{}", courseDisplay, retryContext.getRetryCount());
                  rosterSync(data, false);
                  return null;
               });

            /*
            Compare the end date for the course (use the end date for the term to which the course is assigned unless
            the course has an override end date, in which case use the end date for the course) to the current date.
            If the course end date < current date, mark the course as inactive in gct_course_init.
             */
               boolean isCourseLocked = CourseHelper.isLocked(course, false);
               if (isCourseLocked) {
                  courseInit.setSyncStatus(CourseInit.SYNC_STATUS.INACTIVE);
                  courseInitRepository.save(courseInit);
                  inactivated.add(courseDisplay);
               }
               successes.add(courseDisplay);

            } catch (IOException e) {
               log.error("Error performing roster sync for courseInit: " + courseInit.getId(), e);
               errors.add(courseDisplay);
            }
         } else {
            errors.add(courseId + " is not a legit canvas course in this environment");
         }
      }

      sendBatchNotificationForRosterSync(successes, errors, inactivated);
   }

   private void syncCanvasAndGoogleGroups(List<CourseGroup> canvasCourseGroups, String userLoginToSync) throws IOException {
      log.debug("syncCanvasAndGoogleGroups() for {}", userLoginToSync);
      for (CourseGroup cg : canvasCourseGroups) {
         Group canvasGroup = createGroupForCanvasGroup(cg);
         log.debug("{}", canvasGroup.getEmail());
         List<User> groupUsers = groupsApi.getUsersInGroup(cg.getId(), true);
         List<String> userEmails = groupUsers.stream()
               .map(User::getLoginId)
               .filter(userLoginToSync::equals)
               .map(this::loginToEmail)
               .collect(Collectors.toList());

         //Get just the users with role MEMBER
         List<String> groupMembers = getMembersOfGroup(canvasGroup.getEmail()).stream()
               .filter(m -> m.getRole().equals(GROUP_ROLES.MEMBER.name()))
               .map(Member::getEmail)
               .filter(email -> email.equals(loginToEmail(userLoginToSync)))
               .collect(Collectors.toList());

         List<String> toRemove = (List<String>) CollectionUtils.removeAll(groupMembers, userEmails);

         log.debug("Canvas group ({}) roster: {}", canvasGroup.getEmail(), userEmails);
         log.debug("Google group ({}) roster: {}", canvasGroup.getEmail(), groupMembers);
         log.debug("Users to be removed: {}", toRemove);

         List<String> toAdd = (List<String>) CollectionUtils.removeAll(userEmails, groupMembers);

         for (String userToRemove : toRemove) {
            removeMemberFromGroup(canvasGroup.getEmail(), userToRemove);
         }

         log.debug("Users to be added: {}", toAdd);
         addMembersToGroup(canvasGroup.getEmail(), toAdd, GROUP_ROLES.MEMBER);
      }
   }

   /**
    * Get the canvas groups for the course
    * @param courseId
    * @return
    */
   public List<CourseGroup> getCanvasGroupsForCourse(String courseId) {
      return groupsApi.getGroupsForCourse(courseId);
   }

   public void rosterSync(RosterSyncCourseData courseDetail, boolean sendNotificationForCourse) throws IOException {
      log.debug("Roster sync for course: {} ({})", courseDetail.getCourseTitle(), courseDetail.getCourseId());

      // Get active course roster
      List<User> users = coursesApi.getUsersForCourseByTypeOptionalEnrollments(courseDetail.getCourseId(), null,
            Collections.singletonList(EnrollmentHelper.STATE.active.name()), true);

      List<CourseGroup> canvasCourseGroups = getCanvasGroupsForCourse(courseDetail.getCourseId());

      CourseInit courseInit = getCourseInit(courseDetail.getCourseId());

      //Ensure group settings are correct
      createCourseGroups(courseDetail.getCourseId(), courseDetail.getCourseTitle(), courseInit.getMailingListAddress() != null);

      //Ensure canvas group settings are correct
      List<String> canvasGroupEmails = new ArrayList<>();

      for (CourseGroup cg : canvasCourseGroups) {
         Group canvasGroup = createGroupForCanvasGroup(cg);
         log.debug("Syncing canvas group {}...", canvasGroup.getEmail());
         List<User> groupUsers = groupsApi.getUsersInGroup(cg.getId(), true);
         List<String> userEmails = groupUsers.stream().map(User::getLoginId)
               .map(this::loginToEmail)
               .collect(Collectors.toList());

         canvasGroupEmails.add(canvasGroup.getEmail().toLowerCase());

         //Get just the users with role MEMBER
         List<String> groupMembers = getMembersOfGroup(canvasGroup.getEmail()).stream()
               .filter(m -> m.getRole().equals(GROUP_ROLES.MEMBER.name()))
               .map(Member::getEmail)
               .collect(Collectors.toList());

         List<String> toRemove = (List<String>) CollectionUtils.removeAll(groupMembers, userEmails);
         //Need to make sure that gctadmin doesn't get removed from the group even though it's not in the course's group
         toRemove.remove(toolConfig.getImpersonationAccount());
         //Also need to remove anything that isn't an @iu.edu email since we don't want to manage them
         toRemove.removeIf(email -> !email.endsWith("@iu.edu"));

         log.debug("Canvas group ({}) roster: {}", canvasGroup.getEmail(), userEmails);
         log.debug("Google group ({}) roster: {}", canvasGroup.getEmail(), groupMembers);
         log.debug("Users to be removed: {}", toRemove);

         List<String> toAdd = (List<String>) CollectionUtils.removeAll(userEmails, groupMembers);

         for (String userToRemove : toRemove) {
            removeMemberFromGroup(canvasGroup.getEmail(), userToRemove);
         }

         log.debug("Users to be added: {}", toAdd);
         addMembersToGroup(canvasGroup.getEmail(), toAdd, GROUP_ROLES.MEMBER);
      }

      //Check for canvas groups we don't care about anymore
      List<GroupsInit> groupsInits = groupsInitRepository.findByCanvasCourseIdAndEnv(courseDetail.getCourseId(), toolConfig.getEnv());
      for (GroupsInit gi : groupsInits) {
         String groupEmail = getEmailForCourseGroup(gi.getCanvasCourseId(), gi.getCanvasGroupId());
         if (!canvasGroupEmails.contains(groupEmail.toLowerCase())) {
            List<String> membersOfGroup = getMembersOfGroup(groupEmail).stream().map(Member::getEmail).collect(Collectors.toList());
            //Need to make sure that gctadmin doesn't get removed from the group
            membersOfGroup.remove(toolConfig.getImpersonationAccount());
            log.debug("Removing {} members of the {} group since it no longer exists in Canvas", membersOfGroup.size(), groupEmail);
            for (String toRemove : membersOfGroup) {
               log.debug("Removing {} from {}", toRemove, groupEmail);
               removeMemberFromGroup(groupEmail, toRemove);
            }
         }
      }

      File courseFolder = getFolder(courseInit.getCourseFolderId());

      //Find all regular users
      List<DecoratedCanvasUser> decoratedCanvasUsers = users.stream()
            .filter(u -> verifyUserEligibility(u.getEmail(), u.getLoginId(), u.getSisUserId()))
            .map(DecoratedCanvasUser::new)
            .collect(Collectors.toList());

      Map<String, DecoratedCanvasUser> userMap = decoratedCanvasUsers.stream()
            .collect(Collectors.toMap(DecoratedCanvasUser::getEmail, Function.identity()));
      log.debug("User Map: {}", userMap);

      Set<String> courseEmails = userMap.keySet();
      log.debug("Users (email): {}", courseEmails);

      List<Member> allGroupMembers = getMembersOfGroup(courseDetail.getAllGroupEmail());
      List<String> allGroupEmails = allGroupMembers.stream().map(Member::getEmail).collect(Collectors.toList());
      List<Member> teacherGroupMembers = getMembersOfGroup(courseDetail.getTeacherGroupEmail());
      List<String> teacherGroupEmails = teacherGroupMembers.stream().map(Member::getEmail).collect(Collectors.toList());

      log.debug("Users in ALL: {}", allGroupEmails);
      log.debug("Users in TEACHER: {}", teacherGroupEmails);

      List<String> toRemoveFromAll = (List<String>) CollectionUtils.removeAll(allGroupEmails, courseEmails);
      //Need to make sure that gctadmin doesn't get removed from the group even though it's not in the course
      toRemoveFromAll.remove(toolConfig.getImpersonationAccount());
      //Also need to remove anything that isn't an @iu.edu email since we don't want to manage them
      toRemoveFromAll.removeIf(email -> !email.endsWith("@iu.edu"));
      log.debug("Users to remove from ALL: {}", toRemoveFromAll);

      List<String> missingFromAll = (List<String>) CollectionUtils.removeAll(courseEmails, allGroupEmails);
      log.debug("Users missing from ALL: {}", missingFromAll);

      Map<String, UserInit> userInitMap = new HashMap<>();

      for (String userEmail : toRemoveFromAll) {
         removeMemberFromGroup(courseDetail.getAllGroupEmail(), userEmail);
         UserInit userInit = userInitMap.computeIfAbsent(userEmail, key -> userInitRepository.findByGoogleLoginIdAndEnv(key, toolConfig.getEnv()));
         if (userInit != null) {
            File shortcut = findShortcutForTarget(courseFolder.getName(), courseFolder.getId(), userInit.getFolderId(), null);
            if (shortcut != null) {
               deleteFolder(shortcut.getId());
            }
         }

         //remove dropbox perms if it's a student
         if (courseInit.getDropboxFolderId() != null) {
            DropboxInit dropboxInit = getDropboxInitByGoogleLogin(courseDetail.getCourseId(), userEmail);
            if (dropboxInit != null) {
               deleteFolderPermission(dropboxInit.getFolderId(), userEmail);
            }
         }
      }

      for (String userEmail : missingFromAll) {
         DecoratedCanvasUser decoratedCanvasUser = userMap.get(userEmail);
         GROUP_ROLES groupRole = decoratedCanvasUser.isTeacher() ? GROUP_ROLES.MANAGER : GROUP_ROLES.MEMBER;
         addMemberToGroup(courseDetail.getAllGroupEmail(), userEmail, groupRole);

         //Check to see if the student should have a dropbox but doesn't
         if (decoratedCanvasUser.isStudent() && courseInit.getDropboxFolderId() != null) {
            DropboxInit dropboxInit = getDropboxInit(courseDetail.getCourseId(), decoratedCanvasUser.getLoginId());
            if (dropboxInit == null) {
               dropboxInit = createStudentDropboxFolder(courseDetail.getCourseId(), courseDetail.getCourseTitle(),
                     courseInit.getDropboxFolderId(), decoratedCanvasUser.getLoginId(), courseDetail.getAllGroupEmail(),
                     courseDetail.getTeacherGroupEmail(), dropboxInit);
            }
         }
      }

      List<String> toRemoveFromTeachers = (List<String>) CollectionUtils.removeAll(teacherGroupEmails, courseEmails);
      //Need to make sure that gctadmin doesn't get removed from the group even though it's not in the course
      toRemoveFromTeachers.remove(toolConfig.getImpersonationAccount());
      //Also need to remove anything that isn't an @iu.edu email since we don't want to manage them
      toRemoveFromTeachers.removeIf(email -> !email.endsWith("@iu.edu"));

      //Find any TAs or DEs that should no longer be in the teacher group
      List<String> moreUsersToRemoveFromTeachers = decoratedCanvasUsers.stream()
            .filter(dcu -> teacherGroupEmails.contains(dcu.getEmail()) && !dcu.isTeacher() && ((!courseInit.isDeTeacher() && dcu.isDesigner()) || (!courseInit.isTaTeacher() && dcu.isTa())))
            .map(DecoratedCanvasUser::getEmail)
            .collect(Collectors.toList());
      toRemoveFromTeachers.addAll(moreUsersToRemoveFromTeachers);

      log.debug("Users to remove from TEACHER: {}", toRemoveFromTeachers);

      List<String> filteredCourseInstructors = decoratedCanvasUsers.stream()
            .filter(dcu -> dcu.isTeacher() || (courseInit.isDeTeacher() && dcu.isDesigner()) || (courseInit.isTaTeacher() && dcu.isTa()))
            .map(DecoratedCanvasUser::getEmail)
            .collect(Collectors.toList());
      log.debug("Filtered Instructor types: {}", filteredCourseInstructors);

      List<String> missingFromTeachers = (List<String>) CollectionUtils.removeAll(filteredCourseInstructors, teacherGroupEmails);
      log.debug("Users missing from TEACHER: {}", missingFromTeachers);

      for (String userEmail : toRemoveFromTeachers) {
         removeMemberFromGroup(courseDetail.getTeacherGroupEmail(), userEmail);

         //Also remove from the canvasGroups
         for (String canvasGroupEmail : canvasGroupEmails) {
            removeMemberFromGroup(canvasGroupEmail, userEmail);
         }
      }

      for (String userEmail : missingFromTeachers) {
         DecoratedCanvasUser dcu = userMap.get(userEmail);
         GROUP_ROLES groupRole = dcu.isTeacher() ? GROUP_ROLES.MANAGER : GROUP_ROLES.MEMBER;
         if (dcu.isTeacher() || (dcu.isDesigner() && courseInit.isDeTeacher()) || (dcu.isTa() && courseInit.isTaTeacher())) {
            addMemberToGroup(courseDetail.getTeacherGroupEmail(), userEmail, groupRole);

            //Also add to canvasGroups (always as a MANAGER)
            for (String canvasGroupEmail : canvasGroupEmails) {
               addMemberToGroup(canvasGroupEmail, userEmail, GROUP_ROLES.MANAGER);
            }
         }
      }
      if (sendNotificationForCourse) {
         sendRosterSyncNotification(courseDetail.getCourseId(), courseDetail.getCourseTitle());
      }
   }

   /**
    * Send roster sync notification
    * @param courseId
    * @param courseTitle
    */
   private void sendRosterSyncNotification(String courseId, String courseTitle) {
      List<User> courseInstructors = coursesApi.getUsersForCourseByType(courseId,
            Collections.singletonList(EnrollmentHelper.TYPE.teacher.name()),
            null);

      List<String> courseInstructorIds = courseInstructors.stream()
            .map(User::getId)
            .collect(Collectors.toList());

      String courseLink = MessageFormat.format("{0}/courses/{1}", canvasApi.getBaseUrl(), courseId);

      Map<String, Object> emailModel = new HashMap<>();
      emailModel.put("courseTitle", courseTitle);
      emailModel.put("courseLink", courseLink);

      ConversationCreateWrapper wrapper = new ConversationCreateWrapper();
      wrapper.setRecipients(courseInstructorIds);
      wrapper.setContextCode("course_" + courseId);
      wrapper.setGroupConversation(true);
      wrapper.setSubject("Google Course Tools Roster Sync Completed");
      sendNotification("courseRosterSync.ftlh", emailModel, wrapper);
   }

   /**
    * Send roster sync batch notification
    * @param successes
    * @param errors
    * @param inactivated
    */
   private void sendBatchNotificationForRosterSync(List<String> successes, List<String> errors, List<String> inactivated) {
      Map<String, Object> emailModel = new HashMap<>();

      Date runDate = new Date();
      emailModel.put("runTime" , runDate);
      emailModel.put("env" , toolConfig.getEnv());

      emailModel.put("successes", successes);
      emailModel.put("errors", errors);
      emailModel.put("inactivated", inactivated);

      try {
         Template freemarkerTemplate = freemarkerConfigurer.createConfiguration()
               .getTemplate("batchRosterSync.ftlh");

         String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, emailModel);

         EmailDetails details = new EmailDetails();
         details.addRecipientsItem(toolConfig.getBatchNotificationEmail());

         details.setSubject(emailApi.getStandardHeader() + " GCT Roster Sync");
         details.setBody(body);

         emailApi.sendEmail(details, true);
      } catch (TemplateException | IOException e) {
         log.error("Unable to send batch roster sync email", e);
      }
   }

   /**
    * Build the url to the group based on the email address
    * @param groupEmail Email address that is the key for the group
    * @return Url that points to the group
    */
   public String buildGroupUrlFromEmail(String groupEmail) {
      String groupUrlTemplate = "https://groups.google.com/a/iu.edu/g/{0}/settings";
      String groupIdentifier = groupEmail.substring(0, groupEmail.indexOf("@"));
      return MessageFormat.format(groupUrlTemplate, groupIdentifier);
   }

   /**
    * Wrap the url with something that will force an IU login for that google resource
    * @param url Google resource url that will be wrapped in an auth url
    * @return Full auth url that points at the input google resource url
    */
   public String authWrapUrl(String url) {
      String googleAuthUrlTemplate = toolConfig.getGoogleAuthUrlTemplate();
      return MessageFormat.format(googleAuthUrlTemplate, url);
   }
}
