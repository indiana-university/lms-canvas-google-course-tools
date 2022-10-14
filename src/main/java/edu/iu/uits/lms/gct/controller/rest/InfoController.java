package edu.iu.uits.lms.gct.controller.rest;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.gct.config.ToolConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/info")
@Tag(name = "InfoController", description = "Return helpful information about the tool")
@Slf4j
public class InfoController {

   @Autowired
   private ToolConfig toolConfig;

   @Autowired
   private RabbitProperties rabbitProperties;

   @GetMapping
   @Operation(summary = "Get information about the tool")
   public Config getInfo() {
      Config config = new Config(toolConfig, rabbitProperties);
      return config;
   }

   /**
    * Need this class because I can't return the ToolConfig directly due to beanFactory things also being returned
    */
   @Data
   private static class Config {
      private String version;
      private String env;
      private String impersonationAccount;
      private String domain;
      private String envDisplayPrefix;
      private String pickerApiKey;
      private String pickerClientId;
      private String dropboxQueueName;
      private String rosterSyncQueueName;
      private String batchNotificationEmail;
      private RabbitProps rabbitProps;

      public Config(ToolConfig toolConfig, RabbitProperties rabbitProperties) {
         this.version = toolConfig.getVersion();
         this.env = toolConfig.getEnv();
         this.impersonationAccount = toolConfig.getImpersonationAccount();
         this.domain = toolConfig.getDomain();
         this.envDisplayPrefix = toolConfig.getEnvDisplayPrefix();
         this.pickerApiKey = "******";
         this.pickerClientId = toolConfig.getPickerClientId();
         this.dropboxQueueName = toolConfig.getDropboxQueueName();
         this.rosterSyncQueueName = toolConfig.getRosterSyncQueueName();
         this.batchNotificationEmail = toolConfig.getBatchNotificationEmail();
         this.rabbitProps = new RabbitProps(rabbitProperties.getHost(), rabbitProperties.getUsername(), rabbitProperties.getPort());
      }
   }

   @Data
   @AllArgsConstructor
   private static class RabbitProps {
      private String host;
      private String username;
      private Integer port;
   }

}
