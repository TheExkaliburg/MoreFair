package de.kaliburg.morefair.game.message;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WSUtils;
import de.kaliburg.morefair.game.ladder.LadderRepository;
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
      AccountService accountService, WSUtils wsUtils) {
    this.messageRepository = messageRepository;
    this.accountService = accountService;
  }

  public MessageEntity createMessage() {
    MessageEntity result = new MessageEntity();

    return result;
  }


}
