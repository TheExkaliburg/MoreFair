package de.kaliburg.morefair.chat.services;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * Service that manages the {@link MessageEntity} entities.
 */
public interface MessageService {

  String EMPTY_METADATA = "[]";

  List<MessageEntity> find(ChatEntity chat);

  MessageEntity create(AccountEntity account, ChatEntity chat, String message,
      @Nullable String metadata);

  default MessageEntity create(AccountEntity account, ChatEntity chat, String message) {
    return create(account, chat, message, EMPTY_METADATA);
  }

  List<MessageEntity> findNewestMessagesByChatType(List<ChatType> chatTypes);

  void deleteMessagesOfAccount(AccountEntity account);
}
