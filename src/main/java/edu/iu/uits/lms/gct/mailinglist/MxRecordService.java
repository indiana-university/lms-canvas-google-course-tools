package edu.iu.uits.lms.gct.mailinglist;

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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Slf4j
@Service
public class MxRecordService {

   @Autowired
   @Qualifier("mxRestTemplate")
   private RestTemplate mxRestTemplate;

   @Autowired
   private MxServicesConfig config;

   public MxRecord getMxRecord(String username) {
      MxRecord result = null;

      UriTemplate template = new UriTemplate("{url}/GetIuGroupInfo");
      URI baseUri = template.expand(config.getBaseUrl());

      URI uri = UriComponentsBuilder.fromUri(baseUri)
            .queryParam("groupUsername", username)
            .build().toUri();

      log.debug("{}", uri);
      try {
         HttpEntity<MxRecord> responseEntity = mxRestTemplate.getForEntity(uri, MxRecord.class);
         log.debug("{}", responseEntity);

         result = responseEntity.getBody();
      } catch (RestClientException e) {
         log.warn("Unable to lookup mx record for " + username, e);
      }

      return result;
   }

   public MxRecord createMxRecord(String username) {
      MxRecord result = null;

      UriTemplate template = new UriTemplate("{url}/CreateIuGroupEmail");
      URI baseUri = template.expand(config.getBaseUrl());

      URI uri = UriComponentsBuilder.fromUri(baseUri)
            .queryParam("groupUsername", username)
            .build().toUri();

      log.debug("{}", uri);
      try {
         HttpEntity<MxRecord> responseEntity = mxRestTemplate.postForEntity(uri, null, MxRecord.class);
         log.debug("{}", responseEntity);

         result = responseEntity.getBody();
      } catch (Exception e) {
         log.warn("Unable to create mx record for " + username, e);
      }

      return result;
   }

}
