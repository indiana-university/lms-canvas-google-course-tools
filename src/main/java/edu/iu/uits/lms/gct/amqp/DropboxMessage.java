package edu.iu.uits.lms.gct.amqp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DropboxMessage implements Serializable {
   private String courseId;
   private String courseTitle;
   private String dropboxFolderId;
   private String allGroupEmail;
   private String teacherGroupEmail;
}
