package edu.iu.uits.lms.gct.mailinglist;

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
