package edu.iu.uits.lms.gct.config;

import edu.iu.uits.lms.gct.Constants;
import edu.iu.uits.lms.gct.amqp.DropboxMessageListener;
import edu.iu.uits.lms.gct.amqp.DropboxMessageSender;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

   @Bean
   Queue queue() {
      return new Queue(Constants.DROPBOX_QUEUE);
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
