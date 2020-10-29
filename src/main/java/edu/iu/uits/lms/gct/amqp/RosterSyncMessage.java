package edu.iu.uits.lms.gct.amqp;

import edu.iu.uits.lms.gct.model.RosterSyncCourseData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RosterSyncMessage implements Serializable {
   private RosterSyncCourseData courseData;
   private boolean sendNotificationForCourse;

}
