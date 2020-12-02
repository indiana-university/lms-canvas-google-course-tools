package edu.iu.uits.lms.gct.job;

import edu.iu.uits.lms.gct.services.GoogleCourseToolsService;
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
      }

      ctx.close();
   }
}
