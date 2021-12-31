package de.kaliburg.morefair.controller.chat;

import de.kaliburg.morefair.dto.chat.ChatDTO;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ChatController {

    public ResponseEntity<ChatDTO> getChat(String uuid, Integer ladderNum) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> postChat(String uuid, Integer ladderNum, String message) {
        uuid = StringEscapeUtils.escapeJava(uuid);
        message = StringEscapeUtils.escapeJava(message);

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
