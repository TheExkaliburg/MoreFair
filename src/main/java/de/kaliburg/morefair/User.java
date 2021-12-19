package de.kaliburg.morefair;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User
{
    private String username;
    private int position;
    private int points;
}
