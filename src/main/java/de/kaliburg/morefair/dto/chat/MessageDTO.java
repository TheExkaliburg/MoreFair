package de.kaliburg.morefair.dto.chat;

import de.kaliburg.morefair.entity.chat.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;

@Data
public class MessageDTO {
    private final String message;
    private final String username;
    private final Integer timesAsshole;

    public MessageDTO(Message message) {
        this.timesAsshole = message.getAccount().getTimesAsshole();
        this.message = JavaScriptUtils.javaScriptEscape(HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getMessage())));
        this.username = JavaScriptUtils.javaScriptEscape(HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getAccount().getUsername())));
    }
}
