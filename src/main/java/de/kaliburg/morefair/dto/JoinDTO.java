package de.kaliburg.morefair.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class JoinDTO {
    @NonNull
    private String username;
    @NonNull
    private Integer timesAsshole;
}
