package de.kaliburg.morefair.game.chat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.game.chat.dto.ChatDto;
import jakarta.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;

  private final MessageService messageService;

  private final Semaphore chatSemaphore = new Semaphore(1);

  private final Cache<Pair<ChatType, Integer>, ChatEntity> chatTypeCache =
      Caffeine.newBuilder().build();
  private final Cache<UUID, ChatEntity> chatUuidCache =
      Caffeine.newBuilder().build();
  private final Cache<Long, ChatEntity> chatIdCache =
      Caffeine.newBuilder().build();

  @Override
  public ChatEntity find(@NotNull Long id) {
    ChatEntity result = chatIdCache.getIfPresent(id);
    if (result == null) {
      result = chatRepository.findById(id).orElse(null);
    }
    loadIntoCache(result);

    return result;
  }

  @Override
  public ChatEntity find(@NonNull UUID uuid) {
    ChatEntity result = chatUuidCache.getIfPresent(uuid);
    if (result == null) {
      result = chatRepository.findByUuid(uuid).orElse(null);
    }
    loadIntoCache(result);

    return result;
  }

  @Override
  public ChatEntity find(@NonNull ChatType type, Integer number) {
    if (type.isParameterized() && number == null) {
      throw new NullPointerException("ChatType is parameterized but number is null");
    } else if (!type.isParameterized()) {
      number = null;
    }

    Pair<ChatType, Integer> keyPair = new Pair<>(type, number);
    ChatEntity result = chatTypeCache.getIfPresent(keyPair);

    if (result == null) {
      result = chatRepository.findByTypeAndNumber(type, number).orElse(null);
    }

    if (result == null) {
      log.info("Chat with type {} and number {} not found", type, number);
      result = createChat(type, number);
    }
    loadIntoCache(result);

    return result;
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


  private ChatEntity save(ChatEntity chat) {
    try {
      chatSemaphore.acquire();
      try {
        ChatEntity result = chatRepository.save(chat);

        loadIntoCache(result);

        return result;
      } finally {
        chatSemaphore.release();
      }
    } catch (InterruptedException e) {
      log.error("Failed to acquire chat semaphore", e);
      return chat;
    }
  }

  private void loadIntoCache(ChatEntity chat) {
    if (chat == null) {
      return;
    }

    chatTypeCache.put(new Pair<>(chat.getType(), chat.getNumber()), chat);
    chatUuidCache.put(chat.getUuid(), chat);
    chatIdCache.put(chat.getId(), chat);
  }

  private ChatEntity createChat(ChatType chatType, @Nullable Integer number) {
    ChatEntity chat = new ChatEntity(chatType);

    if (number != null) {
      chat.setNumber(number);
    }

    log.info("Creating new Chat with the identifier: {}", chat.getIdentifier());

    return save(chat);
  }
}
