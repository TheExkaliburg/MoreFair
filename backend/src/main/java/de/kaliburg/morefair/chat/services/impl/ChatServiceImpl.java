package de.kaliburg.morefair.chat.services.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.ChatService;
import de.kaliburg.morefair.chat.services.repositories.ChatRepository;
import jakarta.annotation.Nullable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
  private final LoadingCache<Long, ChatEntity> chatCache;
  private final LoadingCache<Pair<ChatType, Integer>, Long> chatLookup;

  public ChatServiceImpl(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;

    this.chatCache = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(30, ChronoUnit.MINUTES))
        .build(id -> this.chatRepository.findById(id).orElse(null));
    this.chatLookup = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(30, ChronoUnit.MINUTES))
        .build(pair -> this.chatRepository.findByTypeAndNumber(pair.getValue0(), pair.getValue1())
            .map(ChatEntity::getId)
            .orElse(null)
        );
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

    return Optional.ofNullable(chatLookup.get(pair))
        .map(chatCache::get)
        .orElseGet(() -> create(type, finalNumber));
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
