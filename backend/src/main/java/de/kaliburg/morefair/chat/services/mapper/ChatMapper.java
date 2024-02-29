package de.kaliburg.morefair.chat.services.mapper;

import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.ChatDto;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.data.ModChatDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link ChatEntity ChatEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class ChatMapper {

  private final MessageService messageService;
  private final ChatService chatService;
  private final MessageMapper messageMapper;

  /**
   * Mapping the {@link ChatEntity} to a {@link ChatDto}.
   *
   * @param chat the {@link ChatEntity}
   * @return The {@link ChatDto}
   */
  public ChatDto convertToChatDto(@NonNull ChatEntity chat) {
    return ChatDto.builder()
        .type(chat.getType())
        .number(chat.getNumber())
        .messages(
            messageService.find(chat).stream().sorted(MessageEntity::compareTo)
                .map((m) -> messageMapper.convertToMessageDto(m, chat)).toList()
        )
        .build();
  }

  /**
   * Mapping a list of {@link MessageEntity messages} to a {@link ModChatDto}.
   *
   * @param messages the list of {@link MessageEntity messages}.
   * @return the {@link ModChatDto}
   */
  public ModChatDto mapToModChatDto(List<MessageEntity> messages) {
    return ModChatDto.builder()
        .messages(
            messages.stream()
                .map((m) -> messageMapper.convertToMessageDto(m, chatService.find(m.getChatId())))
                .collect(Collectors.toList())
        )
        .build();
  }

}
