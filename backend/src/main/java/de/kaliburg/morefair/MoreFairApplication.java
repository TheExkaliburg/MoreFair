package de.kaliburg.morefair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoreFairApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoreFairApplication.class, args);
  }
}
