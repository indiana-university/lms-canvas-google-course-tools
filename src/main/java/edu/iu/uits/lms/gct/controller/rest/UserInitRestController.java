package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/userinit")
@Slf4j
public class UserInitRestController {

   @Autowired
   private UserInitRepository userInitRepository = null;

   @GetMapping("/{id}")
   public UserInit get(@PathVariable Long id) {
      return userInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/login/{env}/{loginId}")
   public UserInit getByLoginId(@PathVariable String env, @PathVariable String loginId) {
      return userInitRepository.findByLoginIdAndEnv(loginId, env);
   }

   @PutMapping("/{id}")
   public UserInit update(@PathVariable Long id, @RequestBody UserInit userInit) {
      UserInit updatedUserInit = userInitRepository.findById(id).orElse(null);

      if (updatedUserInit != null) {
         if (userInit.getFolderId() != null) {
            updatedUserInit.setFolderId(userInit.getFolderId());
         }
         if (userInit.getGoogleLoginId() != null) {
            updatedUserInit.setGoogleLoginId(userInit.getGoogleLoginId());
         }
         if (userInit.getLoginId() != null) {
            updatedUserInit.setLoginId(userInit.getLoginId());
         }
         if (userInit.getEnv() != null) {
            updatedUserInit.setEnv(userInit.getEnv());
         }

         return userInitRepository.save(updatedUserInit);
      }
      return null;
   }

   @PostMapping("/")
   public UserInit create(@RequestBody UserInit userInit) {
      UserInit newUserInit = UserInit.builder()
            .folderId(userInit.getFolderId())
            .googleLoginId(userInit.getGoogleLoginId())
            .loginId(userInit.getLoginId())
            .env(userInit.getEnv())
            .build();
      return userInitRepository.save(newUserInit);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      userInitRepository.deleteById(id);
      return "Delete success.";
   }

}
