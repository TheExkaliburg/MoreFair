package de.kaliburg.morefair.dto.chat;

import de.kaliburg.morefair.entity.chat.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.HtmlUtils;

@Data
public class MessageDTO {
    private final String message;
    private final String username;

    public MessageDTO(Message message) {
        this.message = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getMessage()));
        this.username = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getAccount().getUsername()));
    }
}
