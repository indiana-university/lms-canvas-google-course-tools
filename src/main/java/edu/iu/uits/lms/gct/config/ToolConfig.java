package edu.iu.uits.lms.gct.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gct")
@Getter
@Setter
public class ToolConfig {

   private String version;
   private String env;
   private String impersonationAccount;
   private String domain;
   private String envDisplayPrefix;
   private String pickerApiKey;
   private String pickerClientId;
   private String dropboxQueueName;
}
