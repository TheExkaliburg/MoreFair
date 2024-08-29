package de.kaliburg.morefair.chat.model.dto;

import de.kaliburg.morefair.chat.model.types.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageDto {

  private final String message;
  private final String username;
  private final Long accountId;
  private final Long timestamp;
  private final String tag;
  private final Integer assholePoints;
  private final String metadata;
  private final Boolean isMod;
  private final ChatType chatType;
  private final Integer ladderNumber;
}
