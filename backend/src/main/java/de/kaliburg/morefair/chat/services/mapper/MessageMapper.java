package de.kaliburg.morefair.chat.services.mapper;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import de.kaliburg.morefair.game.season.model.AchievementsEntity;
import de.kaliburg.morefair.game.season.services.AchievementsService;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The Mapper that can convert the {@link MessageEntity MessageEntities} to DTOs.
 */
@Component
@RequiredArgsConstructor
public class MessageMapper {

  private final AccountService accountService;
  private final AchievementsService achievementsService;
  private final FairConfig fairConfig;

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
    AccountEntity account = accountService.findById(message.getAccountId()).orElseThrow();
    AchievementsEntity achievements =
        achievementsService.findOrCreateByAccountInCurrentSeason(account.getId());

    return MessageDto.builder()
        .message(message.getMessage())
        .metadata(message.getMetadata())
        .username(account.getDisplayName())
        .accountId(account.getId())
        .assholePoints(achievements.getAssholePoints())
        .tag(fairConfig.getAssholeTag(achievements.getAssholeLevel()))
        .isMod(account.isMod())
        .timestamp(message.getCreatedOn().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond())
        .chatType(chat.getType())
        .ladderNumber(chat.getNumber())
        .build();
  }
}
