package edu.iu.uits.lms.gct.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "gct")
@PropertySource(value = {"classpath:env.properties",
      "classpath:default.properties",
      "classpath:application.properties",
      "${app.fullFilePath}/lms.properties",
      "${app.fullFilePath}/protected.properties",
      "${app.fullFilePath}/security.properties"}, ignoreResourceNotFound = true)


@Getter
@Setter
public class ToolConfig {

   private String version;
   private String impersonationAccount;
   private String domain;
   private String rootFolderId;
   private String coursesFolderId;
   private String usersFolderId;
}
