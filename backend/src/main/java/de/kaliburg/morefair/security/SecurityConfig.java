package de.kaliburg.morefair.security;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableRedisHttpSession
public class SecurityConfig {

  private final AccountService accountService;
  private final PasswordEncoder passwordEncoder;
  private final RedisTokenRepositoryImpl redisTokenRepository;
  private final FairConfig config;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors().disable();
    http.csrf(csrf -> csrf
        .csrfTokenRepository(new CustomCookieCsrfTokenRepository())
        .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler()::handle)
    );
    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    );
    http.securityContext(securityContext -> securityContext
        .requireExplicitSave(false)
    );
    http.addFilter(usernamePasswordAuthenticationFilter());
    http.authorizeHttpRequests((requests) -> requests
        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/**"))
        .permitAll()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/**"))
        .permitAll()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/socket/fair"))
        .permitAll()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/api/**"))
        .authenticated()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/**"))
        .authenticated()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/**"))
        .authenticated()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/**"))
        .authenticated()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PATCH, "/api/**"))
        .authenticated()

        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/**"))
        .authenticated()

        .anyRequest()
        .permitAll()
    );
    http.rememberMe().rememberMeServices(rememberMeServices());
    http.logout().logoutUrl("/api/auth/logout").logoutSuccessUrl("/");
    http.exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

    return http.build();
  }

  @Bean
  public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() {
    UsernamePasswordAuthenticationFilter authLoginFilter =
        new UsernamePasswordAuthenticationFilter(authenticationManager());
    authLoginFilter.setRequiresAuthenticationRequestMatcher(
        new AntPathRequestMatcher("/api/auth/login", "POST"));
    authLoginFilter.setRememberMeServices(rememberMeServices());
    authLoginFilter.setContinueChainBeforeSuccessfulAuthentication(true);
    authLoginFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());

    return authLoginFilter;
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(accountService);
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
  public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setSameSite(SameSiteCookies.STRICT.getValue());
    return serializer;
  }


}
