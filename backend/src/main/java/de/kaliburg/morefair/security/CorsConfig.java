package de.kaliburg.morefair.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

  /*
  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    //config.addAllowedOriginPattern("^http://localhost:[0-9]+$|^http://127.0.0.1:[0-9]+$");
    config.addAllowedOrigin("http://localhost:3000");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter());
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    bean.setUrlPatterns(Arrays.asList("/*"));
    bean.setEnabled(true);

    return bean;
  }
  */

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("*")
            .maxAge(3600);
      }
    };
  }

}
