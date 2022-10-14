package edu.iu.uits.lms.gct.services;

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

import edu.iu.uits.lms.gct.config.ToolConfig;
import edu.iu.uits.lms.gct.controller.rest.PropertiesRestController;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import edu.iu.uits.lms.lti.config.TestUtils;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PropertiesRestController.class}, properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@Slf4j
@ActiveProfiles("none")
public class RestLaunchSecurityTest {
   @Autowired
   private MockMvc mvc;

   @MockBean
   private GctPropertyRepository gctPropertyRepository;

   @Test
   public void restNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/properties/test/foo")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
   }

   @Test
   public void restAuthnLaunch() throws Exception {
      Jwt jwt = createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/rest/properties/test/foo")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(token)))
            .andExpect(status().isOk());
   }

   @Test
   public void restAuthnLaunchWithWrongScope() throws Exception {
      Jwt jwt = createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/rest/properties/test/foo")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(token)))
            .andExpect(status().isForbidden());
   }

   public static Jwt createJwtToken(String username) {
      Jwt jwt = Jwt.withTokenValue("fake-token")
            .header("typ", "JWT")
            .header("alg", SignatureAlgorithm.RS256.getValue())
            .claim("user_name", username)
            .claim("client_id", username)
            .notBefore(Instant.now())
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .subject(username)
            .build();

      return jwt;
   }
}
