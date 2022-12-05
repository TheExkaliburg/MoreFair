package de.kaliburg.morefair.game.chat;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.game.round.LadderRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * The MessageService that setups and manages the Messages contained in the ChatEntity
 */
@Service
@Log4j2
public class MessageService {

  private final MessageRepository messageRepository;
  private final AccountService accountService;


  public MessageService(MessageRepository messageRepository, LadderRepository ladderRepository,
      AccountService accountService) {
    this.messageRepository = messageRepository;
    this.accountService = accountService;
  }

  /**
   * Create and saves a new message.
   *
   * @param account  the account from where the message originiated
   * @param chat     the chat where its written
   * @param message  the message
   * @param metadata the metadata of that message
   * @return the saved message entity
   */
  public MessageEntity create(AccountEntity account, ChatEntity chat, String message,
      String metadata) {
    if (metadata == null || metadata.isBlank()) {
      return create(account, chat, message);
    }

    MessageEntity result = new MessageEntity(account, message, chat);
    result.setMetadata(metadata);

    return save(result);
  }

  /**
   * Creates and saves a new message.
   *
   * @return the message
   */
  public MessageEntity create(AccountEntity account, ChatEntity chat, String message) {
    MessageEntity result = new MessageEntity(account, message, chat);
    return save(result);
  }

  @Transactional
  public MessageEntity save(MessageEntity message) {
    return messageRepository.save(message);
  }

  public MessageEntity find(Long id) {
    return messageRepository.findById(id).orElseThrow();
  }

  public MessageEntity find(UUID uuid) {
    return messageRepository.findByUuid(uuid).orElseThrow();
  }

  public ArrayList<MessageEntity> getAllMessages() {
    return new ArrayList<>();
  }

  public List<MessageEntity> loadMessages(ChatEntity chat) {
    return messageRepository.findTop30ByChatAndDeletedOnNullOrderByCreatedOnDesc(chat);
  }

  @Transactional
  public List<MessageEntity> save(List<MessageEntity> messages) {
    return messageRepository.saveAll(messages);
  }

  /**
   * Sets all messages of a specific account as deleted.
   *
   * @param account the account that needs their messages deleted
   */
  public void deleteMessagesOfAccount(AccountEntity account) {
    List<MessageEntity> messages = messageRepository.findByAccount(account);

    messages.forEach(message -> {
      message.setDeletedOn(OffsetDateTime.now());
    });

    messageRepository.saveAll(messages);
  }
}
