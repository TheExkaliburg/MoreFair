package de.kaliburg.morefair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoreFairApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoreFairApplication.class, args);
  }

  @Bean
  public Argon2PasswordEncoder bcryptPasswordEncoder() {
    return new Argon2PasswordEncoder();
  }
}
