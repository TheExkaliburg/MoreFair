package de.kaliburg.morefair.game.chat;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.game.chat.dto.MessageDto;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class MessageServiceImpl implements MessageService {

  private final FairConfig config;
  private final MessageRepository messageRepository;
  private final WsUtils wsUtils;
  private final Semaphore cacheSemaphore = new Semaphore(1);
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
    return getMessageEntityFromCache(messageIdCache, id);
  }

  @Override
  public MessageEntity find(@NonNull UUID uuid) {
    return getMessageEntityFromCache(messageUuidCache, uuid);
  }

  @Override
  public List<MessageEntity> find(@NonNull ChatEntity chat) {
    try {
      cacheSemaphore.acquire();
      try {
        List<MessageEntity> result = messagesChatIdCache.get(chat.getId());
        if (result == null || result.isEmpty()) {
          return result;
        }
        messagesChatIdCache.put(result.get(0).getChat().getId(), result);
        return result;
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
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

    try {
      cacheSemaphore.acquire();
      try {
        result = messageRepository.save(result);
        loadIntoCache(result, true);
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }

    wsUtils.convertAndSendToTopic("/chat/" + chat.getIdentifier(), convertToDto(result));

    return result;
  }

  @Override
  public List<MessageEntity> findNewestMessagesByChatType(ChatType chatType) {
    return messageRepository.findNewestMessagesByChatType(chatType);
  }

  public void deleteMessagesOfAccount(AccountEntity account) {
    messageRepository.setDeletedOnForAccount(account, OffsetDateTime.now());
    try {
      cacheSemaphore.acquire();
      try {
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
        .build();
  }

  private void loadIntoCache(MessageEntity message) {
    loadIntoCache(message, false);
  }

  private void loadIntoCache(MessageEntity message, boolean addIntoLists) {
    if (message == null) {
      return;
    }
    messageUuidCache.put(message.getUuid(), message);
    messageIdCache.put(message.getId(), message);

    if (addIntoLists) {
      List<MessageEntity> chatMessages = messagesChatIdCache.get(message.getChat().getId());

      if (chatMessages.stream().anyMatch(m -> m.getId().equals(message.getId()))) {
        //Replace old message with new one
        chatMessages = chatMessages.stream()
            .map(m -> m.getId().equals(message.getId()) ? message : m).toList();
      } else {
        //Add new message to cache and delete last one if over limit
        chatMessages.add(0, message);
        chatMessages.sort(MessageEntity::compareTo);
        if (chatMessages.size() > MessageRepository.MESSAGES_PER_PAGE) {
          chatMessages.remove(chatMessages.size() - 1);
        }
      }
      messagesChatIdCache.put(message.getChat().getId(), chatMessages);
    }
  }

  private <K, V extends MessageEntity> V getMessageEntityFromCache(LoadingCache<K, V> cache,
      K key) {
    try {
      cacheSemaphore.acquire();
      try {
        V result = cache.get(key);
        loadIntoCache(result);
        return result;
      } finally {
        cacheSemaphore.release();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }
}
