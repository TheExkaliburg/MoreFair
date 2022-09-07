package de.kaliburg.morefair.security;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserDetailsService userDetailsService;
  private final Argon2PasswordEncoder argon2PasswordEncoder;

  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
        authenticationManager());
    customAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");

    http.csrf().disable();
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/auth/**").permitAll();
    http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/auth/**").permitAll();
    http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.PUT, "/api/**").authenticated();
    http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/api/**").authenticated();
    http.authorizeRequests().anyRequest().authenticated();
    http.addFilterBefore(new CustomAuthorizationFilter(),
        UsernamePasswordAuthenticationFilter.class);
    http.addFilter(customAuthenticationFilter);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(argon2PasswordEncoder);
  }
}
