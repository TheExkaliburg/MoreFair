package de.kaliburg.morefair.security;

import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserDetailsWithUuid extends User {

  @Getter
  private final UUID uuid;

  public UserDetailsWithUuid(String username, String password,
      Collection<? extends GrantedAuthority> authorities, UUID uuid) {
    super(username, password, authorities);
    this.uuid = uuid;
  }
}
