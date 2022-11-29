package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.ChatController;
import de.kaliburg.morefair.api.utils.WsUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ChatService that setups and manages the GameEntity. Also routes all the requests for the
 * Chats themselves.
 */
@Service
@Log4j2
public class ChatService {

  private final ChatRepository chatRepository;
  private final MessageService messageService;
  private final WsUtils wsUtils;
  private final AccountService accountService;
  private final FairConfig config;
  @Getter(AccessLevel.PACKAGE)
  private final Semaphore chatSemaphore = new Semaphore(1);
  private Map<Integer, ChatEntity> currentChatMap = new HashMap<>();

  public ChatService(ChatRepository chatRepository, MessageService messageService,
      @Lazy WsUtils wsUtils, AccountService accountService, FairConfig config) {
    this.chatRepository = chatRepository;
    this.messageService = messageService;
    this.wsUtils = wsUtils;
    this.accountService = accountService;
    this.config = config;
  }

  @Transactional
  public void saveStateToDatabase() {
    try {
      chatSemaphore.acquire();
      try {
        for (ChatEntity chat : currentChatMap.values()) {
          messageService.save(chat.getMessages());
        }
        chatRepository.saveAll(currentChatMap.values());
      } finally {
        chatSemaphore.release();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new Chat for the game with the specific number, saves it in the db and adds it to the
   * cache.
   *
   * @param number the number of the Chat.
   * @return the newly created and saved Chat
   */
  @Transactional
  public ChatEntity create(Integer number) {
    ChatEntity result = chatRepository.save(new ChatEntity(number));
    currentChatMap.put(result.getNumber(), result);
    return result;
  }

  @Transactional
  public List<ChatEntity> save(List<ChatEntity> chats) {
    return chatRepository.saveAll(chats);
  }

  /**
   * Overwrites the existing cached chats with the ones from this game.
   */
  public void loadIntoCache() {
    currentChatMap = new HashMap<>();
    chatRepository.findAll().forEach(chat -> {
      chat.setMessages(messageService.loadMessages(chat));
      currentChatMap.put(chat.getNumber(), chat);
    });
  }

  @Transactional
  ChatEntity save(ChatEntity chat) {
    return chatRepository.save(chat);
  }

  ChatEntity find(Long id) {
    ChatEntity chat = chatRepository.findById(id).orElse(null);
    if (chat != null) {
      chat.setMessages(messageService.loadMessages(chat));
    }
    return chat;
  }

  ChatEntity find(UUID uuid) {
    ChatEntity chat = chatRepository.findByUuid(uuid).orElse(null);
    if (chat != null) {
      chat.setMessages(messageService.loadMessages(chat));
    }
    return chat;
  }

  ChatEntity findByNumber(Long number) {
    ChatEntity chat = chatRepository.findByNumber(number).orElse(null);
    if (chat != null) {
      chat.setMessages(messageService.loadMessages(chat));
    }
    return chat;
  }

  /**
   * Sends a message (and the metadata) from a user to a specific chat.
   *
   * @param account  the account of the user
   * @param number   the number of the chat
   * @param message  the message
   * @param metadata the metadata of the message
   * @return the message entity
   */
  public MessageEntity sendMessageToChat(AccountEntity account, Integer number,
      String message, String metadata) {
    try {
      chatSemaphore.acquire();
      try {
        ChatEntity cachedChat = find(number);
        MessageEntity result = messageService.create(account, cachedChat, message, metadata);

        cachedChat.getMessages().add(0, result);

        if (cachedChat.getMessages().size() >= 50) {
          cachedChat.getMessages().remove(cachedChat.getMessages().size() - 1);
        }

        result = messageService.save(result);
        wsUtils.convertAndSendToTopic(
            ChatController.TOPIC_EVENTS_DESTINATION.replace("{number}", number.toString()),
            new MessageDto(result, config));
        return result;
      } finally {
        chatSemaphore.release();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends a message without metadata from a user to a specific chat.
   *
   * @param account the account of the user
   * @param number  the number of the chat
   * @param message the message
   * @return the message entity
   */
  public MessageEntity sendMessageToChat(AccountEntity account, Integer number, String message) {
    return sendMessageToChat(account, number, message, "[]");
  }

  /**
   * Sends a message (and the metadata) from a user to all chats.
   *
   * @param message  the message
   * @param metadata the metadata of the message
   * @return the list of all the messages, sent to different chats
   */
  public List<MessageEntity> sendGlobalMessage(String message, String metadata) {
    try {
      chatSemaphore.acquire();
      try {
        AccountEntity broadcasterAccount = accountService.findBroadcaster();
        return currentChatMap.values().stream()
            .map(chat -> sendMessageToChat(broadcasterAccount, chat.getNumber(), message, metadata))
            .collect(Collectors.toList());
      } finally {
        chatSemaphore.release();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends a message (and the metadata) from a user to all chats.
   *
   * @param message the message
   * @return the list of all the messages, sent to different chats
   */
  public List<MessageEntity> sendGlobalMessage(String message) {
    return sendGlobalMessage(message, "[]");
  }


  /**
   * gets the instance of a chat, if there is no cached chat, it will first look for a chat version
   * from the database or create one if there is none.
   *
   * @param number the number the chat has
   * @return the Chat Entity from the cache
   */
  public ChatEntity find(Integer number) {
    if (currentChatMap.isEmpty()) {
      return null;
    }
    ChatEntity result = currentChatMap.get(number);
    if (result == null) {
      result = create(number);
    }
    result.setMessages(messageService.loadMessages(result));

    return result;
  }

  @Transactional
  public void deleteMessagesOfAccount(AccountEntity account) {
    try {
      chatSemaphore.acquire();
      try {
        for (ChatEntity chat : currentChatMap.values()) {
          messageService.save(chat.getMessages());
        }
        chatRepository.saveAll(currentChatMap.values());
        messageService.deleteMessagesOfAccount(account);
        loadIntoCache();
      } finally {
        chatSemaphore.release();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }


}
