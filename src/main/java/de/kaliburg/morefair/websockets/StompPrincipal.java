package de.kaliburg.morefair.websockets;

import lombok.Data;
import lombok.NonNull;

import java.security.Principal;
import java.util.UUID;

@Data
public class StompPrincipal implements Principal {
    @NonNull
    private String name;

    public StompPrincipal(@NonNull String name) {
        this.name = name;
    }

    public StompPrincipal() {
        this.name = UUID.randomUUID().toString();
    }
}
