package edu.iu.uits.lms.gct.controller.rest;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import edu.iu.uits.lms.gct.model.UserInit;
import edu.iu.uits.lms.gct.repository.UserInitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "UserInitRestController", description = "Interact with the UserInit repository with CRUD operations")
@Slf4j
public class UserInitRestController {

   @Autowired
   private UserInitRepository userInitRepository = null;

   @GetMapping("/{id}")
   @Operation(summary = "Get a UserInit by id")
   public UserInit get(@PathVariable Long id) {
      return userInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/login/{env}/{loginId}")
   @Operation(summary = "Get a UserInit by env and loginId")
   public UserInit getByLoginId(@PathVariable String env, @PathVariable String loginId) {
      return userInitRepository.findByLoginIdAndEnv(loginId, env);
   }

   @PutMapping("/{id}")
   @Operation(summary = "Update a UserInit by id")
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
   @Operation(summary = "Create a new UserInit")
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
   @Operation(summary = "Delete a UserInit by id")
   public String delete(@PathVariable Long id) {
      userInitRepository.deleteById(id);
      return "Delete success.";
   }

}
