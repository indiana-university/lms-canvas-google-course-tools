package edu.iu.uits.lms.gct.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

import static edu.iu.uits.lms.lti.LTIConstants.BASE_USER_ROLE;
import static edu.iu.uits.lms.lti.LTIConstants.WELL_KNOWN_ALL;

@Configuration
public class SecurityConfig {

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 5)
    public static class GctRestSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/**", "/api/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/rest/**")
                  .access("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')")
                  .antMatchers("/api/**").permitAll()
                  .and()
                  .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                  .and()
                  .oauth2ResourceServer()
                  .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4)
    public static class GctWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                  .requestMatchers()
                  .antMatchers(WELL_KNOWN_ALL, "/error", "/app/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers(WELL_KNOWN_ALL, "/error").permitAll()
                  .antMatchers("/**").hasRole(BASE_USER_ROLE);

            //Setup the LTI handshake
            Lti13Configurer lti13Configurer = new Lti13Configurer()
                  .grantedAuthoritiesMapper(new CustomRoleMapper(defaultInstructorRoleRepository));

            http.apply(lti13Configurer);

            //Fallback for everything else
            http.requestMatchers().antMatchers("/**")
                  .and()
                  .authorizeRequests()
                  .anyRequest().authenticated();

            http.exceptionHandling().accessDeniedPage("/accessDenied");
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore everything except paths specified
            web.ignoring().antMatchers("/app/jsrivet/**", "/app/webjars/**", "/actuator/**", "/app/css/**", "/app/js/**", "/favicon.ico");
        }

    }


    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class GctCatchAllSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/**")
                  .and()
                  .authorizeRequests()
                  .anyRequest().authenticated();
        }
    }
}
