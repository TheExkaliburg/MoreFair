package de.kaliburg.morefair.moderation.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModServerMessageData {
    private Long id;
    private String location;
    private String content;
    private String event;
}
