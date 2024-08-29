package de.kaliburg.morefair.chat.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.MessageDto;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.chat.services.mapper.MessageMapper;
import de.kaliburg.morefair.chat.services.repositories.MessageRepository;
import de.kaliburg.morefair.core.concurrency.CriticalRegion;
import de.kaliburg.morefair.utils.FormattingUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class MessageServiceImpl implements MessageService {

  private final CriticalRegion semaphore = new CriticalRegion(1);
  private final MessageRepository messageRepository;
  private final ChatService chatService;
  private final WsUtils wsUtils;
  private final LoadingCache<Long, List<MessageEntity>> messagesChatIdCache;
  private final MessageMapper messageMapper;

  public MessageServiceImpl(MessageRepository messageRepository, ChatService chatService,
      WsUtils wsUtils, MessageMapper messageMapper) {
    this.messageRepository = messageRepository;
    this.chatService = chatService;
    this.wsUtils = wsUtils;
    this.messageMapper = messageMapper;

    messagesChatIdCache = Caffeine.newBuilder()
        .build(this.messageRepository::findNewestMessagesByChatId);
  }

  @Override
  public List<MessageEntity> find(@NonNull ChatEntity chat) {
    try (var ignored = semaphore.enter()) {
      return messagesChatIdCache.get(chat.getId());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
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

    try (var ignored = semaphore.enter()) {
      result = save(result);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    MessageDto dto = messageMapper.convertToMessageDto(result, chat);
    wsUtils.convertAndSendToTopic("/chat/events/" + chat.getDestination(), dto);
    wsUtils.convertAndSendToTopic("/moderation/chat/events", dto);

    return result;
  }

  @Override
  public List<MessageEntity> findNewestMessagesByChatType(List<ChatType> chatTypes) {
    // No Need for caching here, as this is only used for the chat history
    return messageRepository.findNewestMessagesByChatTypes(chatTypes);
  }

  @Override
  public void deleteMessagesOfAccount(AccountEntity account) {
    try (var ignored = semaphore.enter()) {
      messageRepository.setDeletedOnForAccount(account.getId(), OffsetDateTime.now(ZoneOffset.UTC));
      messagesChatIdCache.invalidateAll();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }


  private MessageEntity save(MessageEntity message) {
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

  }
}
