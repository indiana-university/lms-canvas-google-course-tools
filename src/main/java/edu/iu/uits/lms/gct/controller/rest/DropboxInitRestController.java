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
