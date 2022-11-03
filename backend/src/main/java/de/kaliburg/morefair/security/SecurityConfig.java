package de.kaliburg.morefair.security;

import de.kaliburg.morefair.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableRedisHttpSession
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final AccountService accountService;
  private final Argon2PasswordEncoder argon2PasswordEncoder;

  private final Secrets secrets;

  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
        authenticationManager(), accountService);
    customAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");

    http.csrf().csrfTokenRepository(new CustomCookieCsrfTokenRepository());
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/auth/**").permitAll();
    http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/auth/**").permitAll();
    http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.PUT, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.PATCH, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/api/**").authenticated();
    http.authorizeRequests().anyRequest().permitAll();
    http.addFilter(customAuthenticationFilter);
    /*http.rememberMe().key(secrets.getRememberMeKey()).tokenValiditySeconds(86400 * 30)
        .alwaysRemember(true).useSecureCookie(true).rememberMeCookieName("rememberMe");*/
    http.logout().logoutUrl("/api/auth/logout").logoutSuccessUrl("/");
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(accountService).passwordEncoder(argon2PasswordEncoder);
  }
}
