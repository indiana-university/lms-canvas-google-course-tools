package edu.iu.uits.lms.gct.job;

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

import edu.iu.uits.lms.common.batch.BatchJob;
import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
import edu.iu.uits.lms.iuonly.model.errorcontact.ErrorContactPostForm;
import edu.iu.uits.lms.iuonly.services.ErrorContactServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Profile("rostersync")
public class RosterSyncJob implements BatchJob {

   private GoogleCourseToolsService googleCourseToolsService;
   private ConfigurableApplicationContext ctx;

   @Autowired
   private RosterSyncJob job;

   @Autowired
   private ErrorContactServiceImpl errorContactService;

   public RosterSyncJob(GoogleCourseToolsService googleCourseToolsService, ConfigurableApplicationContext ctx) {
      this.googleCourseToolsService = googleCourseToolsService;
      this.ctx = ctx;
   }

   private void rosterSync() throws IOException {
      log.info("RosterSync job running!");
      googleCourseToolsService.rosterSyncBatch();
   }

   @Override
   public void run() {

      try {
         job.rosterSync();
      } catch (Exception e) {
         log.error("Caught exception performing roster sync", e);

         ErrorContactPostForm errorContactPostForm = new ErrorContactPostForm();
         errorContactPostForm.setJobCode(getJobCode());
         errorContactPostForm.setMessage("The Roster Synchronization job has unexpectedly failed");

         errorContactService.postEvent(errorContactPostForm);
      }

      ctx.close();
   }

   public String getJobCode() {
      return "RosterSyncJob";
   }
}
