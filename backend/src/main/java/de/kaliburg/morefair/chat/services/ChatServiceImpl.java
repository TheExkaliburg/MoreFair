package de.kaliburg.morefair.chat.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import de.kaliburg.morefair.chat.model.ChatEntity;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.dto.ChatDto;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.repositories.ChatRepository;
import de.kaliburg.morefair.core.caching.MultiIndexedLoadingCache;
import de.kaliburg.morefair.data.ModChatDto;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
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
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;
  private final MessageService messageService;
  private MultiIndexedLoadingCache<Long, ChatEntity> chatCache;


  public ChatServiceImpl(ChatRepository chatRepository, @Lazy MessageService messageService) {
    this.chatRepository = chatRepository;
    this.messageService = messageService;

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

  @Override
  public ChatDto convertToChatDto(@NonNull ChatEntity chat) {
    return ChatDto.builder()
        .type(chat.getType())
        .number(chat.getNumber())
        .messages(
            messageService.find(chat).stream().sorted(MessageEntity::compareTo)
                .map((m) -> messageService.convertToMessageDto(m, chat)).toList()
        )
        .build();
  }

  @Override
  public ModChatDto convertToModChatDto(List<MessageEntity> messages) {
    return ModChatDto.builder()
        .messages(
            messages.stream()
                .map((m) -> messageService.convertToMessageDto(m, find(m.getChatId())))
                .collect(Collectors.toList())
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
