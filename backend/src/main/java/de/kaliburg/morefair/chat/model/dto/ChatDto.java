package de.kaliburg.morefair.chat.model.dto;

import de.kaliburg.morefair.chat.model.types.ChatType;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class ChatDto {

  private final ChatType type;
  private final Integer number;
  @Builder.Default
  private final List<MessageDto> messages = new ArrayList<>();

}
