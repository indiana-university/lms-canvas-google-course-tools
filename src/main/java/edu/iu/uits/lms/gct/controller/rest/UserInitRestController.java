package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest/userinit")
@Slf4j
@Api(tags = "userinit")
@PreAuthorize("#oauth2.hasScope('lms:rest')")
public class UserInitRestController {

   @Autowired
   private UserInitRepository userInitRepository = null;

   @GetMapping("/{id}")
   public UserInit get(@PathVariable Long id) {
      return userInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/login/{loginId}")
   public List<UserInit> getByLoginId(@PathVariable String loginId) {
      return userInitRepository.findByLoginId(loginId);
   }

   @PutMapping("/{id}")
   public UserInit update(@PathVariable Long id, @RequestBody UserInit userInit) {
      UserInit updatedDropboxInit = userInitRepository.findById(id).orElse(null);

      if (updatedDropboxInit != null) {
         if (userInit.getFolderId() != null) {
            updatedDropboxInit.setFolderId(userInit.getFolderId());
         }
         if (userInit.getGoogleLoginId() != null) {
            updatedDropboxInit.setGoogleLoginId(userInit.getGoogleLoginId());
         }
         if (userInit.getLoginId() != null) {
            updatedDropboxInit.setLoginId(userInit.getLoginId());
         }

         return userInitRepository.save(updatedDropboxInit);
      }
      return null;
   }

   @PostMapping("/")
   public UserInit create(@RequestBody UserInit userInit) {
      UserInit newCourseInit = UserInit.builder()
            .folderId(userInit.getFolderId())
            .googleLoginId(userInit.getGoogleLoginId())
            .loginId(userInit.getLoginId())
            .build();
      return userInitRepository.save(newCourseInit);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      userInitRepository.deleteById(id);
      return "Delete success.";
   }

}
