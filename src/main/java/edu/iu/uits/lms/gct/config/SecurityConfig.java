package edu.iu.uits.lms.gct.config;

import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@Slf4j
public class SecurityConfig {

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
    public static class GctLtiSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        public static final String PATH_TO_OPEN = "/lti";

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                  .antMatcher(PATH_TO_OPEN)
                  .authorizeRequests()
                  .anyRequest()
                  .permitAll();

            //Need to disable csrf so that we can use POST via REST
            http.csrf().disable();

            //Need to disable the frame options so we can embed this in another tool
            http.headers().frameOptions().disable();
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class GctRestWebSecurityConfigurationAdapter extends ResourceServerConfigurerAdapter {
        public static final String ANT_PATH_TO_MATCH = "/rest/**";

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                  .antMatcher(ANT_PATH_TO_MATCH)
                  .authorizeRequests()
                  .anyRequest().authenticated();
        }
    }

    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static class GctWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        public static final String PATH_TO_SECURE = "/app/**";

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authenticationProvider(new LtiAuthenticationProvider());
            http.authorizeRequests(authorizeRequests ->
                  authorizeRequests
                        .antMatchers(PATH_TO_SECURE).hasRole(LtiAuthenticationProvider.LTI_USER)
                        .anyRequest().authenticated());

            //Need to disable csrf so that we can use POST via REST
            http.csrf().disable();

            //Need to disable the frame options so we can embed this in another tool
            http.headers().frameOptions().disable();

            http.exceptionHandling().accessDeniedPage("/accessDenied");
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore everything except paths specified
            web.ignoring().antMatchers("/templates/**", "/app/jsreact/**", "/app/static/**", "/app/webjars/**",
                  "/resources/**", "/actuator/**", "/app/css/**", "/app/js/**");
        }

    }
}
