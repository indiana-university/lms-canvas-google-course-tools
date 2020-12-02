package edu.iu.uits.lms.gct.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

   @Autowired
   private ToolConfig toolConfig = null;

   @Bean(name = "dropboxQueue")
   Queue dropboxQueue() {
      return new Queue(toolConfig.getDropboxQueueName());
   }

   @Bean(name = "rosterSyncQueue")
   Queue rosterSyncQueue() {
      return new Queue(toolConfig.getRosterSyncQueueName());
   }

}
