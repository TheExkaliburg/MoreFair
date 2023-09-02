package de.kaliburg.morefair.game.chat;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.core.AbstractCacheableService;
import de.kaliburg.morefair.game.chat.dto.MessageDto;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
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
  private final WsUtils wsUtils;
  private final LoadingCache<Long, List<MessageEntity>> messagesChatIdCache;
  private final LoadingCache<UUID, MessageEntity> messageUuidCache;
  private final LoadingCache<Long, MessageEntity> messageIdCache;

  public MessageServiceImpl(MessageRepository messageRepository, @Lazy FairConfig config,
      @Lazy WsUtils wsUtils) {
    this.config = config;
    this.messageRepository = messageRepository;
    this.wsUtils = wsUtils;

    messagesChatIdCache =
        Caffeine.newBuilder().build(messageRepository::findNewestMessagesByChatId);
    messageUuidCache = Caffeine.newBuilder().maximumSize(1024)
        .build(uuid -> messageRepository.findByUuid(uuid).orElse(null));
    messageIdCache = Caffeine.newBuilder().maximumSize(1024)
        .build(id -> messageRepository.findById(id).orElse(null));
  }

  @Override
  public MessageEntity find(@NonNull Long id) {
    return getValueFromCacheSync(messageIdCache, id);
  }

  @Override
  public MessageEntity find(@NonNull UUID uuid) {
    return getValueFromCacheSync(messageUuidCache, uuid);
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

    MessageEntity result = new MessageEntity(account, message, chat);
    if (metadata != null && !metadata.isBlank()) {
      result.setMetadata(metadata);
    }

    result = save(result);

    if (result != null) {
      wsUtils.convertAndSendToTopic("/chat/events/" + chat.getDestination(), convertToDto(result));
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
    try {
      cacheSemaphore.acquire();
      try {
        messageRepository.setDeletedOnForAccount(account, OffsetDateTime.now());
        messageIdCache.invalidateAll();
        messageUuidCache.invalidateAll();
        messagesChatIdCache.invalidateAll();
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public MessageDto convertToDto(MessageEntity message) {
    return MessageDto.builder()
        .message(message.getMessage())
        .metadata(message.getMetadata())
        .username(message.getAccount().getDisplayName())
        .accountId(message.getAccount().getId())
        .assholePoints(message.getAccount().getAssholePoints())
        .tag(config.getAssholeTag(message.getAccount().getAssholeCount()))
        .isMod(message.getAccount().isMod())
        .timestamp(message.getCreatedOn().withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond())
        .chatType(message.getChat().getType())
        .build();
  }


  private MessageEntity save(MessageEntity message) {
    try {
      cacheSemaphore.acquire();
      try {
        MessageEntity result = messageRepository.save(message);

        List<MessageEntity> chatMessages = messagesChatIdCache.get(result.getChat().getId());
        if (chatMessages.stream().anyMatch(m -> m.getId().equals(result.getId()))) {
          //Replace old message with new one
          chatMessages = chatMessages.stream()
              .map(m -> m.getId().equals(result.getId()) ? result : m).toList();
        } else {
          //Add new message to cache and delete last one if over limit
          chatMessages.add(0, result);
          chatMessages.sort(MessageEntity::compareTo);
          if (chatMessages.size() > MessageRepository.MESSAGES_PER_PAGE) {
            chatMessages.remove(chatMessages.size() - 1);
          }
        }
        messagesChatIdCache.put(result.getChat().getId(), chatMessages);

        if (messageIdCache.getIfPresent(result.getId()) != null) {
          messageIdCache.put(result.getId(), result);
        }
        if (messageUuidCache.getIfPresent(result.getUuid()) != null) {
          messageUuidCache.put(result.getUuid(), result);
        }

        return result;
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }
}
