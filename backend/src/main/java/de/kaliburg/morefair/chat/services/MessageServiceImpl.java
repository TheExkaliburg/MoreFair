package de.kaliburg.morefair.chat.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.repositories.MessageRepository;
import de.kaliburg.morefair.core.AbstractCacheableService;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class MessageServiceImpl extends AbstractCacheableService implements MessageService {

  private final FairConfig config;
  private final MessageRepository messageRepository;
  private final AccountService accountService;
  private final ChatService chatService;
  private final WsUtils wsUtils;
  private final LoadingCache<Long, List<MessageEntity>> messagesChatIdCache;

  public MessageServiceImpl(MessageRepository messageRepository, @Lazy FairConfig config,
      AccountService accountService, @Lazy ChatService chatService, @Lazy WsUtils wsUtils) {
    this.config = config;
    this.messageRepository = messageRepository;
    this.accountService = accountService;
    this.chatService = chatService;
    this.wsUtils = wsUtils;

    messagesChatIdCache =
        Caffeine.newBuilder().build(messageRepository::findNewestMessagesByChatId);
  }

  @Override
  public List<MessageEntity> find(@NonNull ChatEntity chat) {
    return getValueFromCacheSync(messagesChatIdCache, chat.getId());
  }

  @Override
  public MessageEntity create(@NonNull AccountEntity account, @NonNull ChatEntity chat,
      @NonNull String message, String metadata) {
    if (message.isBlank()) {
      throw new IllegalArgumentException(
          FormattingUtils.format("Account {} (#{}): Tried to send empty message to chat '{}'",
              account.getDisplayName(), account.getId(), chat.getIdentifier()));
    }

    if ((chat.getType() == ChatType.MOD && !account.isMod())
        || (chat.getType() == ChatType.SYSTEM && !account.isBroadcaster())
        || account.isMuted()) {
      throw new IllegalArgumentException(FormattingUtils.format("Account {} (#{}): Tried to send "
              + "message to chat '{}' without the necessary permissions.",
          account.getDisplayName(), account.getId(), chat.getIdentifier()));
    }

    MessageEntity result = new MessageEntity(account.getId(), message, chat.getId());
    if (metadata != null && !metadata.isBlank()) {
      result.setMetadata(metadata);
    }

    result = save(result);

    if (result != null) {
      MessageDto dto = convertToMessageDto(result, chat);
      wsUtils.convertAndSendToTopic("/chat/events/" + chat.getDestination(), dto);
      wsUtils.convertAndSendToTopic("/moderation/chat/events", dto);
    }

    return result;
  }

  @Override
  public List<MessageEntity> findNewestMessagesByChatType(List<ChatType> chatTypes) {
    // No Need for caching here, as this is only used for the chat history
    return messageRepository.findNewestMessagesByChatTypes(chatTypes);
  }

  @Override
  public void deleteMessagesOfAccount(AccountEntity account) {
    try (var ignored = cacheSemaphore.enter()) {
      messageRepository.setDeletedOnForAccount(account.getId(), OffsetDateTime.now());
      messagesChatIdCache.invalidateAll();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public MessageDto convertToMessageDto(MessageEntity message, ChatEntity chat) {
    AccountEntity accountEntity = accountService.find(message.getAccountId());

    return MessageDto.builder()
        .message(message.getMessage())
        .metadata(message.getMetadata())
        .username(accountEntity.getDisplayName())
        .accountId(accountEntity.getId())
        .assholePoints(accountEntity.getAssholePoints())
        .tag(config.getAssholeTag(accountEntity.getAssholeCount()))
        .isMod(accountEntity.isMod())
        .timestamp(message.getCreatedOn().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond())
        .chatType(chat.getType())
        .ladderNumber(chat.getNumber())
        .build();
  }


  private MessageEntity save(MessageEntity message) {
    try (var ignored = cacheSemaphore.enter()) {
      MessageEntity result = messageRepository.save(message);
      ChatEntity chat = chatService.find(result.getChatId());

      List<MessageEntity> chatMessages = messagesChatIdCache.get(chat.getId());
      if (chatMessages.stream().anyMatch(m -> m.getId().equals(result.getId()))) {
        //Replace old message with new one
        chatMessages = chatMessages.stream()
            .map(m -> m.getId().equals(result.getId()) ? result : m).collect(Collectors.toList());
      } else {
        //Add new message to cache and delete last one if over limit
        chatMessages.add(0, result);
        chatMessages.sort(MessageEntity::compareTo);
        if (chatMessages.size() > MessageRepository.MESSAGES_PER_PAGE) {
          chatMessages.remove(chatMessages.size() - 1);
        }
      }
      messagesChatIdCache.put(chat.getId(), chatMessages);

      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }
}
