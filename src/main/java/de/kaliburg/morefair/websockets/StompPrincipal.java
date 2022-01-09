package de.kaliburg.morefair.websockets;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Data
@RequiredArgsConstructor
public class StompPrincipal implements Principal {
    @NonNull
    private String name;
}
