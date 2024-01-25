package de.kaliburg.morefair.data;

import de.kaliburg.morefair.chat.model.dto.MessageDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ModChatDto {

  @Builder.Default
  private final List<MessageDto> messages = new ArrayList<>();
}
