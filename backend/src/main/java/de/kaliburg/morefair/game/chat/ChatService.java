package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.game.GameEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
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

  private Map<Integer, ChatEntity> currentChatMap = new HashMap<>();

  public ChatService(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;
  }

  /**
   * Creates a new Chat for the game with the specific number, saves it in the db and adds it to the
   * cache.
   *
   * @param parent the parent Game, that this Chat is part of
   * @param number the number of the Chat.
   * @return the newly created and saved Chat
   */
  @Transactional
  public ChatEntity createChat(GameEntity parent, Integer number) {
    ChatEntity result = chatRepository.save(new ChatEntity(parent, number));
    currentChatMap.put(result.getNumber(), result);
    return result;
  }

  /**
   * Updates existing ChatEntities and saves them.
   *
   * @param chats the ChatEntities that need to be updated
   * @return the updated and saved ChatEntities
   */
  @Transactional
  public List<ChatEntity> updateChats(List<ChatEntity> chats) {
    return chatRepository.saveAll(chats);
  }

  /**
   * Overwrites the existing cached chats with the ones from this game.
   *
   * @param game the game that will have its current round cached
   */
  public void loadIntoCache(GameEntity game) {
    currentChatMap = new HashMap<>();
    game.getChats().forEach(chat -> currentChatMap.put(chat.getNumber(), chat));
  }
}
