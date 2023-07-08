package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.game.chat.dto.ChatDto;
import java.util.UUID;
import lombok.NonNull;

/**
 * Service that manages the {@link ChatEntity} entities.
 */
public interface ChatService {

  ChatEntity find(@NonNull Long id);

  ChatEntity find(@NonNull UUID uuid);

  ChatEntity find(@NonNull ChatType type, Integer number);

  ChatDto convertToDto(@NonNull ChatEntity chat);

  default ChatEntity find(@NonNull ChatType chatType) {
    if (chatType.isParameterized()) {
      throw new IllegalArgumentException("ChatType cannot be parameterized if no parameter is "
          + "provided");
    }
    return find(chatType, null);
  }
}
