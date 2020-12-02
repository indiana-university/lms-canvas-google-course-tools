package edu.iu.uits.lms.gct.amqp;

import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "${gct.rosterSyncQueueName}")
@Component
@Profile("!batch")
@Slf4j
public class RosterSyncMessageListener {

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @RabbitHandler
   public void receive(RosterSyncMessage message) {
      log.info("Received <{}>", message);

      try {
         googleCourseToolsService.rosterSync(message.getCourseData(), message.isSendNotificationForCourse());
      } catch (IOException e) {
         log.error("Error performing roster sync", e);
      }
   }

}
