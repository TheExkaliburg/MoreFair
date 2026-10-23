package de.kaliburg.morefair.security;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.services.AccountService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
//@EnableRedisHttpSession(redisNamespace = "session:test")
public class SecurityConfig {

  private final AccountService accountService;
  private final PasswordEncoder passwordEncoder;
  private final RedisTokenRepositoryImpl redisTokenRepository;
  private final CookieSameSiteFilter cookieSameSiteFilter;
  private final FairConfig config;

  @Value("${spring.profiles.active}")
  private String activeProfile;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    http.csrf(csrf -> csrf
        .spa()
        .csrfTokenRepository(new CustomCookieCsrfTokenRepository())
    );
    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    );
    http.formLogin(form -> form
        .loginPage("/login")
        .loginProcessingUrl("/api/auth/login")
        .successHandler(authenticationSuccessHandler())
        .failureHandler(new SimpleUrlAuthenticationFailureHandler())
    );
    http.authorizeHttpRequests((requests) -> requests
        .requestMatchers(HttpMethod.GET, "/login", "/api/auth/**", "/api/_content/**",
            "/api/stats/**", "/socket/fair").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()

        .requestMatchers(HttpMethod.OPTIONS, "/api/**").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
        .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
        .requestMatchers(HttpMethod.PATCH, "/api/**").authenticated()
        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()

        .anyRequest()
        .permitAll()
    );
    http.rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices()));
    http.logout(logout -> logout
        .logoutUrl("/api/auth/logout")
        .invalidateHttpSession(true)
        .logoutSuccessHandler(logoutSuccessHandler())
        .deleteCookies("remember-me")
    );
    http.headers(headers -> headers
        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
        .contentSecurityPolicy(csp ->
            csp.policyDirectives("frame-ancestors 'self' https://galaxy.click")));
    http.addFilterBefore(cookieSameSiteFilter, CsrfFilter.class);
    http.exceptionHandling(exceptions ->
        exceptions.authenticationEntryPoint(authenticationEntryPoint()));

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(accountService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authProvider);
  }

  @Bean
  public RememberMeServices rememberMeServices() {
    CustomPersistentTokenBasedRememberMeServices rememberMeService =
        new CustomPersistentTokenBasedRememberMeServices(config.getSecrets().getRememberMe(),
            accountService, redisTokenRepository);
    rememberMeService.setTokenValiditySeconds(86400 * RedisTokenRepositoryImpl.TOKEN_VALID_DAYS);
    rememberMeService.setUseSecureCookie(true);
    rememberMeService.setAlwaysRemember(false);
    return rememberMeService;
  }

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return new CustomAuthenticationSuccessHandler();
  }

  @Bean
  public LogoutSuccessHandler logoutSuccessHandler() {
    return new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK);
  }

  @Bean
  public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new CustomCookieSerializer();
    serializer.setSameSite(SameSiteCookies.STRICT.getValue());
    serializer.setUseSecureCookie(true);
    return serializer;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList("http://localhost:3000", "http://localhost:8080", "https://fair.kaliburg.de",
            "https://fairtest.kaliburg.de", "https://galaxy.click"));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  AuthenticationEntryPoint authenticationEntryPoint() {
    return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
  }

}
