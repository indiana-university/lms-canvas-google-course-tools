package edu.iu.uits.lms.gct.mailinglist;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gct.mx")
@Getter
@Setter
public class MxServicesConfig {

    private String baseUrl;
    private String token;
}
