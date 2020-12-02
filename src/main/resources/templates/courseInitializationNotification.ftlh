The Google Course Tools LTI has been initialized in ${notificationData.courseTitle} to facilitate collaboration and sharing among course members.

The following Google groups have been created:
- ${notificationData.teacherGroup.name} (email: ${notificationData.teacherGroup.email})
- ${notificationData.allGroup.name} (email: ${notificationData.allGroup.email})

The following folder(s) have been created:
- ${notificationData.rootCourseFolder} (parent folder for all shared folders and files associated with this course)
<#if notificationData.instructorFilesFolder??>
- ${notificationData.instructorFilesFolder} (folder for sharing content with other instructors)
</#if>
<#if notificationData.courseFilesFolder??>
- ${notificationData.courseFilesFolder} (folder for sharing content with the entire class)
</#if>
<#if notificationData.dropboxFilesFolder??>
- ${notificationData.dropboxFilesFolder} (folder containing student drop boxes)
</#if>
<#if notificationData.fileRepositoryFolder??>
- ${notificationData.fileRepositoryFolder} (folder for class members to share content with one another)
</#if>

<#if notificationData.mailingListAddress??>
The following mailing list has been created:
- List name: ${notificationData.mailingListName}
- List email address: ${notificationData.mailingListAddress}
- List URL: https://groups.google.com/a/iu.edu/forum/#!forum/${notificationData.mailingListAddress?keep_before("@iu.edu")}

</#if>
For more information, see: https://kb.iu.edu/d/bgjk
