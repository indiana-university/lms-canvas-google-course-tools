package edu.iu.uits.lms.gct.amqp;

import com.rabbitmq.client.Channel;
import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "${gct.dropboxQueueName}")
@Profile("!batch")
@Component
@Slf4j
public class DropboxMessageListener {

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @RabbitHandler
   public void receive(DropboxMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
      log.info("Received <{}>", message);

      try {
         // ack the message
         channel.basicAck(deliveryTag, false);

         googleCourseToolsService.createStudentDropboxFolders(message.getCourseId(), message.getCourseTitle(),
               message.getDropboxFolderId(), message.getAllGroupEmail(), message.getTeacherGroupEmail());
      } catch (IOException e) {
         log.error("Error creating student dropboxes", e);
      }
   }

}
