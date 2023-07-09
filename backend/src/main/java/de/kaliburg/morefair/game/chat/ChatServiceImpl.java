package de.kaliburg.morefair.game.chat;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.core.AbstractCacheableService;
import de.kaliburg.morefair.game.chat.dto.ChatDto;
import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class ChatServiceImpl extends AbstractCacheableService implements ChatService {

  private final ChatRepository chatRepository;

  private final MessageService messageService;

  private final LoadingCache<Pair<ChatType, Integer>, ChatEntity> chatTypeCache;
  private final LoadingCache<UUID, ChatEntity> chatUuidCache;
  private final LoadingCache<Long, ChatEntity> chatIdCache;


  public ChatServiceImpl(ChatRepository chatRepository, @Lazy MessageService messageService) {
    this.chatRepository = chatRepository;
    this.messageService = messageService;

    chatTypeCache = Caffeine.newBuilder()
        .build(pair -> chatRepository.findByTypeAndNumber(pair.getValue0(), pair.getValue1())
            .orElse(null));
    chatUuidCache = Caffeine.newBuilder()
        .build(uuid -> chatRepository.findByUuid(uuid).orElse(null));
    chatIdCache = Caffeine.newBuilder()
        .build(id -> chatRepository.findById(id).orElse(null));
  }

  @Override
  public ChatEntity find(@NotNull Long id) {
    return getValueFromCacheSync(chatIdCache, id);
  }

  @Override
  public ChatEntity find(@NonNull UUID uuid) {
    return getValueFromCacheSync(chatUuidCache, uuid);
  }

  @Override
  public ChatEntity find(@NonNull ChatType type, Integer number) {
    if (type.isParameterized() && number == null) {
      throw new NullPointerException("ChatType is parameterized but number is null");
    } else if (!type.isParameterized()) {
      number = null;
    }

    Pair<ChatType, Integer> pair = new Pair<>(type, number);
    try {
      cacheSemaphore.acquire();
      try {
        ChatEntity result = chatTypeCache.get(pair);
        if (result == null) {
          log.info("Chat with type {} and number {} not found", type, number);
          result = create(type, number);
          chatTypeCache.put(pair, result);
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

  @Override
  public ChatDto convertToDto(@NonNull ChatEntity chat) {
    return ChatDto.builder()
        .type(chat.getType())
        .number(chat.getNumber())
        .messages(
            messageService.find(chat).stream().sorted(MessageEntity::compareTo)
                .map(messageService::convertToDto).toList()
        )
        .build();
  }

  private ChatEntity create(ChatType chatType, @Nullable Integer number) {
    ChatEntity chat = new ChatEntity(chatType);

    if (number != null) {
      chat.setNumber(number);
    }

    log.info("Creating new Chat with the identifier: {}", chat.getIdentifier());

    return chatRepository.save(chat);
  }
}
