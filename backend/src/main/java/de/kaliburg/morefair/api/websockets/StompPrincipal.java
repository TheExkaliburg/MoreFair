package de.kaliburg.morefair.api.websockets;

import java.security.Principal;
import lombok.Data;
import lombok.NonNull;

@Data
public class StompPrincipal implements Principal {

  @NonNull
  private String name;
  @NonNull
  private Integer ipAddress;

  public StompPrincipal(@NonNull String name, @NonNull Integer ipAddress) {
    this.name = name;
    this.ipAddress = ipAddress;
  }
}
