package de.kaliburg.morefair.api;

import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(WebProperties.class)
@RequiredArgsConstructor
public class StaticFrontendConfig implements WebMvcConfigurer {

  private final WebProperties webProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String[] locations = webProperties.getResources().getStaticLocations();
    String[] nuxtLocations = Arrays.stream(locations)
        .map(location -> location + "_nuxt/")
        .toArray(String[]::new);

    // Nuxt emits hash-only JS filenames and name.hash.extension filenames for other assets.
    registry.addResourceHandler("/_nuxt/{hash:[A-Za-z0-9_-]{8}}.js",
            "/_nuxt/{name:.*\\.[A-Za-z0-9_-]{8}\\.[^.]+}")
        .addResourceLocations(nuxtLocations)
        .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
    registry.addResourceHandler("/**")
        .addResourceLocations(locations)
        .setCacheControl(CacheControl.noCache());
  }
}
