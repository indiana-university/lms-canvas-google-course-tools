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

@RabbitListener(queues = "${gct.rosterSyncQueueName}")
@Component
@Profile("!batch")
@Slf4j
public class RosterSyncMessageListener {

   @Autowired
   private GoogleCourseToolsService googleCourseToolsService;

   @RabbitHandler
   public void receive(RosterSyncMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
      log.info("Received <{}>", message);

      try {
         // ack the message
         channel.basicAck(deliveryTag, false);

         googleCourseToolsService.rosterSync(message.getCourseData(), message.isSendNotificationForCourse());
      } catch (IOException e) {
         log.error("Error performing roster sync", e);
      }
   }

}
