package de.kaliburg.morefair.chat.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link MessageEntity MessageEntities} to DTOs.
 */
@Component
public class MessageMapper {

  private final AccountService accountService;
  private final FairConfig fairConfig;

  public MessageMapper(AccountService accountService, FairConfig fairConfig) {
    this.accountService = accountService;
    this.fairConfig = fairConfig;
  }

  /**
   * Mapping the {@link MessageEntity} to a {@link MessageDto}.
   *
   * <p>This also needs a {@link ChatEntity} since this function is primarily used in places where
   * you already have access to the chat to save a lot of repeat calls when converting all the
   * messages of a chat.
   *
   * @param message The {@link MessageEntity}
   * @param chat    The {@link ChatEntity}
   * @return The {@link MessageDto}
   */
  public MessageDto convertToMessageDto(MessageEntity message, ChatEntity chat) {
    AccountEntity accountEntity = accountService.find(message.getAccountId());

    return MessageDto.builder()
        .message(message.getMessage())
        .metadata(message.getMetadata())
        .username(accountEntity.getDisplayName())
        .accountId(accountEntity.getId())
        .assholePoints(accountEntity.getAssholePoints())
        .tag(fairConfig.getAssholeTag(accountEntity.getAssholeCount()))
        .isMod(accountEntity.isMod())
        .timestamp(message.getCreatedOn().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond())
        .chatType(chat.getType())
        .ladderNumber(chat.getNumber())
        .build();
  }
}
