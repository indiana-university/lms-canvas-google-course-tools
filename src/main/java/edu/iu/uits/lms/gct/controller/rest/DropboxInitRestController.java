package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.DropboxInit;
import edu.iu.uits.lms.gct.repository.DropboxInitRepository;
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

import java.util.List;

@RestController
@RequestMapping("/rest/dropboxinit")
@Slf4j
public class DropboxInitRestController {

   @Autowired
   private DropboxInitRepository dropboxInitRepository = null;

   @GetMapping("/{id}")
   public DropboxInit get(@PathVariable Long id) {
      return dropboxInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/course/{env}/{courseId}")
   public List<DropboxInit> getByCourseId(@PathVariable String env, @PathVariable String courseId) {
      return dropboxInitRepository.findByCourseIdAndEnv(courseId, env);
   }

   @GetMapping("/login/{env}/{loginId}")
   public List<DropboxInit> getByLoginId(@PathVariable String env, @PathVariable String loginId) {
      return dropboxInitRepository.findByLoginIdAndEnv(loginId, env);
   }

   @GetMapping("/{courseId}/{env}/{loginId}")
   public DropboxInit getByCourseAndLogin(@PathVariable String env, @PathVariable String courseId, @PathVariable String loginId) {
      return dropboxInitRepository.findByCourseIdAndLoginIdAndEnv(courseId, loginId, env);
   }

   @PutMapping("/{id}")
   public DropboxInit update(@PathVariable Long id, @RequestBody DropboxInit dropboxInit) {
      DropboxInit updatedDropboxInit = dropboxInitRepository.findById(id).orElse(null);

      if (updatedDropboxInit != null) {
         if (dropboxInit.getCourseId() != null) {
            updatedDropboxInit.setCourseId(dropboxInit.getCourseId());
         }
         if (dropboxInit.getFolderId() != null) {
            updatedDropboxInit.setFolderId(dropboxInit.getFolderId());
         }
         if (dropboxInit.getGoogleLoginId() != null) {
            updatedDropboxInit.setGoogleLoginId(dropboxInit.getGoogleLoginId());
         }
         if (dropboxInit.getLoginId() != null) {
            updatedDropboxInit.setLoginId(dropboxInit.getLoginId());
         }
         if (dropboxInit.getEnv() != null) {
            updatedDropboxInit.setEnv(dropboxInit.getEnv());
         }

         return dropboxInitRepository.save(updatedDropboxInit);
      }
      return null;
   }

   @PostMapping("/")
   public DropboxInit create(@RequestBody DropboxInit dropboxInit) {
      DropboxInit newDropboxInit = DropboxInit.builder()
            .courseId(dropboxInit.getCourseId())
            .folderId(dropboxInit.getFolderId())
            .googleLoginId(dropboxInit.getGoogleLoginId())
            .loginId(dropboxInit.getLoginId())
            .env(dropboxInit.getEnv())
            .build();
      return dropboxInitRepository.save(newDropboxInit);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      dropboxInitRepository.deleteById(id);
      return "Delete success.";
   }

}
