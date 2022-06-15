package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.GroupsInit;
import edu.iu.uits.lms.gct.repository.GroupsInitRepository;
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
@RequestMapping("/rest/groupsinit")
@Slf4j
public class GroupsInitRestController {

   @Autowired
   private GroupsInitRepository groupsInitRepository = null;

   @GetMapping("/{id}")
   public GroupsInit get(@PathVariable Long id) {
      return groupsInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/course/{env}/{courseId}/{groupId}")
   public GroupsInit getByCourseIdGroupId(@PathVariable String env, @PathVariable String courseId, @PathVariable String groupId) {
      return groupsInitRepository.findByCanvasCourseIdAndCanvasGroupIdAndEnv(courseId, groupId, env);
   }

   @PutMapping("/{id}")
   public GroupsInit update(@PathVariable Long id, @RequestBody GroupsInit groupsInit) {
      GroupsInit updatedGroupsInit = groupsInitRepository.findById(id).orElse(null);

      if (updatedGroupsInit != null) {
         if (groupsInit.getCanvasCourseId() != null) {
            updatedGroupsInit.setCanvasCourseId(groupsInit.getCanvasCourseId());
         }
         if (groupsInit.getCanvasGroupId() != null) {
            updatedGroupsInit.setCanvasGroupId(groupsInit.getCanvasGroupId());
         }
         if (groupsInit.getFolderId() != null) {
            updatedGroupsInit.setFolderId(groupsInit.getFolderId());
         }
         if (groupsInit.getEnv() != null) {
            updatedGroupsInit.setEnv(groupsInit.getEnv());
         }

         return groupsInitRepository.save(updatedGroupsInit);
      }
      return null;
   }

   @PostMapping("/")
   public GroupsInit create(@RequestBody GroupsInit groupsInit) {
      GroupsInit newGroupsInit = GroupsInit.builder()
            .canvasCourseId(groupsInit.getCanvasCourseId())
            .canvasGroupId(groupsInit.getCanvasGroupId())
            .folderId(groupsInit.getFolderId())
            .env(groupsInit.getEnv())
            .build();
      return groupsInitRepository.save(newGroupsInit);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      groupsInitRepository.deleteById(id);
      return "Delete success.";
   }
}
