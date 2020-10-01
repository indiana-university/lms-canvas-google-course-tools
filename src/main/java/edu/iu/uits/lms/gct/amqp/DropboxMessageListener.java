package edu.iu.uits.lms.gct.amqp;

import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@RabbitListener(queues = "${gct.dropboxQueueName}")
@Slf4j
public class DropboxMessageListener {

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @RabbitHandler
   public void receive(DropboxMessage message) {
      log.info("Received <{}>", message);

      try {
         googleCourseToolsService.createStudentDropboxFolders(message.getCourseId(), message.getCourseTitle(),
               message.getDropboxFolderId(), message.getAllGroupEmail(), message.getTeacherGroupEmail());
      } catch (IOException e) {
         log.error("Error creating student dropboxes", e);
      }
   }

}
