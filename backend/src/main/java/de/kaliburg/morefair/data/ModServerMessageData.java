package de.kaliburg.morefair.data;

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
