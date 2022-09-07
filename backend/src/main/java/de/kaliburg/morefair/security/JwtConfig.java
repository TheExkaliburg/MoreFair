package de.kaliburg.morefair.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "jwt")
@ConfigurationPropertiesScan
@Data
class JwtConfig {

  private String secret = "";
}
