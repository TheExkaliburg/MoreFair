package de.kaliburg.morefair.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "secret")
@ConfigurationPropertiesScan
@Data
class Secrets {

  private String rememberMeKey = "";
}
