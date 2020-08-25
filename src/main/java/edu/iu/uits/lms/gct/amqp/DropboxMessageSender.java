package edu.iu.uits.lms.gct.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DropboxMessageSender {

   @Autowired
   private RabbitTemplate template;

   @Autowired
   private Queue queue;

   public void send(DropboxMessage message) {
      log.info("Sending message to queue");
      template.convertAndSend(queue.getName(), message);
   }
}
