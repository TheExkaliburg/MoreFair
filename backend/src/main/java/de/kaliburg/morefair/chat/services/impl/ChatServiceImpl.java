package de.kaliburg.morefair.chat.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.repositories.ChatRepository;
import de.kaliburg.morefair.core.caching.MultiIndexedLoadingCache;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;
  private final MultiIndexedLoadingCache<Long, ChatEntity> chatCache;

  public ChatServiceImpl(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;

    chatCache = MultiIndexedLoadingCache.builder(
        Caffeine.newBuilder(),
        ChatEntity::getId,
        id -> chatRepository.findById(id).orElse(null)
    ).addLookupWithOptional(Pair.class,
        chatEntity -> Pair.with(chatEntity.getType(), chatEntity.getNumber()),
        pair -> {
          Pair<ChatType, Integer> typedPair = (Pair<ChatType, Integer>) pair;
          return chatRepository.findByTypeAndNumber(
              typedPair.getValue0(),
              typedPair.getValue1()
          );
        }
    ).build();
  }

  @Override
  public ChatEntity find(@NotNull Long id) {
    return chatCache.get(id);
  }

  @Override
  public ChatEntity find(@NonNull ChatType type, Integer number) {
    if (type.isParameterized() && number == null) {
      throw new NullPointerException("ChatType is parameterized but number is null");
    } else if (!type.isParameterized()) {
      number = 0;
    }

    final int finalNumber = number;
    final Pair<ChatType, Integer> pair = new Pair<>(type, number);

    return chatCache.put(() -> {
      ChatEntity temp = chatCache.lookup(pair);
      if (temp == null) {
        log.info("Chat with type {} and number {} not found", type, finalNumber);
        return create(type, finalNumber);
      }
      return temp;
    });

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
