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
