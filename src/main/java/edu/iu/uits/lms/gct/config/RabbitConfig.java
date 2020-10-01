package edu.iu.uits.lms.gct.config;

import edu.iu.uits.lms.gct.amqp.DropboxMessageListener;
import edu.iu.uits.lms.gct.amqp.DropboxMessageSender;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

   @Autowired
   private ToolConfig toolConfig = null;

   @Bean
   Queue queue() {
      return new Queue(toolConfig.getDropboxQueueName());
   }

   @Bean
   public DropboxMessageListener dropboxMessageListener() {
      return new DropboxMessageListener();
   }

   @Bean
   public DropboxMessageSender dropboxMessageSender() {
      return new DropboxMessageSender();
   }

}
