package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.amqp.DropboxMessage;
import edu.iu.uits.lms.gct.amqp.DropboxMessageSender;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/queue")
@Slf4j
@Api(tags = "queue")
public class MessageQueue {

   @Autowired
   private DropboxMessageSender dropboxMessageSender;

   @PostMapping("/dropbox")
   public ResponseEntity<String> sendMessage(@RequestBody DropboxMessage dropboxMessage) {
      dropboxMessageSender.send(dropboxMessage);
      return ResponseEntity.ok("Message sent to queue");
   }

}
