package de.kaliburg.morefair.api;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.security.CookieSameSiteFilter;
import de.kaliburg.morefair.security.RedisTokenRepositoryImpl;
import de.kaliburg.morefair.security.SecurityConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class StaticFrontendTest {

  @TempDir
  static Path assets;
  private AnnotationConfigWebApplicationContext context;
  private MockMvc mvc;

  @BeforeEach
  void setUp() throws Exception {
    Files.createDirectories(assets.resolve("__nuxt_content/pages"));
    Files.createDirectories(assets.resolve("_nuxt"));
    Files.writeString(assets.resolve("__nuxt_content/pages/sql_dump.txt"), "CREATE TABLE pages;");
    Files.write(assets.resolve("_nuxt/sqlite3.wasm"), new byte[]{0, 97, 115, 109, 1, 0, 0, 0});
    context = new AnnotationConfigWebApplicationContext();
    context.setServletContext(new MockServletContext());
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
        "spring.profiles.active=test");
    context.register(StaticConfiguration.class);
    context.refresh();
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"/rules", "/privacy", "/impressum", "/contributions", "/changelog"})
  void forwardsPublicContentRoutesWithoutAuthentication(String path) throws Exception {
    mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/"));
  }

  @Test
  void servesContentDatabaseAndWasmWithTheExistingSecurityPolicy() throws Exception {
    mvc.perform(get("/__nuxt_content/pages/sql_dump.txt"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("text/plain"))
        .andExpect(content().string("CREATE TABLE pages;"))
        .andExpect(header().string("Content-Security-Policy",
            "frame-ancestors 'self' https://galaxy.click"));
    mvc.perform(get("/_nuxt/sqlite3.wasm"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/wasm"))
        .andExpect(content().bytes(new byte[]{0, 97, 115, 109, 1, 0, 0, 0}));
    mvc.perform(get("/api/account")).andExpect(status().isUnauthorized());
  }

  @Configuration
  @EnableWebMvc
  @Import({FairController.class, SecurityConfig.class})
  static class StaticConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/**").addResourceLocations(assets.toUri().toString());
    }

    @Bean
    AccountService accountService() {
      return mock(AccountService.class);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
      return mock(PasswordEncoder.class);
    }

    @Bean
    RedisTokenRepositoryImpl redisTokenRepository() {
      return mock(RedisTokenRepositoryImpl.class);
    }

    @Bean
    CookieSameSiteFilter cookieSameSiteFilter() {
      return new CookieSameSiteFilter();
    }

    @Bean
    FairConfig fairConfig() {
      FairConfig config = new FairConfig();
      FairConfig.Secrets secrets = new FairConfig.Secrets();
      secrets.setRememberMe("static-resource-test");
      config.setSecrets(secrets);
      return config;
    }
  }
}
