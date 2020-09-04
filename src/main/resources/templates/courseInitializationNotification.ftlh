The Google Course Tools LTI has been initialized in ${notificationData.courseTitle} to facilitate collaboration and sharing among course members.

The following Google groups have been created:
- ${notificationData.teacherGroup.name} (${notificationData.teacherGroup.email})
- ${notificationData.allGroup.name} (${notificationData.allGroup.email})

The following folder(s) have been created:
- ${notificationData.rootCourseFolder} (Parent folder for all shared folders and files associated with this course)
<#if notificationData.instructorFilesFolder??>
- ${notificationData.instructorFilesFolder} (Folder for instructors to share files and folders with the entire class)
</#if>
<#if notificationData.courseFilesFolder??>
- ${notificationData.courseFilesFolder} (Parent folder for shared folders belonging to the course)
</#if>
<#if notificationData.dropboxFilesFolder??>
- ${notificationData.dropboxFilesFolder} (Parent folder for student drop boxes)
</#if>
<#if notificationData.fileRepositoryFolder??>
- ${notificationData.fileRepositoryFolder} (Folder for files and folders shared by class members)
</#if>

<#if notificationData.mailingListAddress??>
The following mailing list has been created:
- List name: ${notificationData.mailingListName}
- List email address: ${notificationData.mailingListAddress}
- List URL: https://groups.google.com/a/iu.edu/forum/#!forum/${notificationData.mailingListAddress?keep_before("@iu.edu")}

</#if>
For more information, see: https://kb.iu.edu/d/bgjk
