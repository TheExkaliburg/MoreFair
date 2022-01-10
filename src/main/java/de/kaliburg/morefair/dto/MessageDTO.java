package de.kaliburg.morefair.dto;

import de.kaliburg.morefair.persistence.entity.Message;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.util.HtmlUtils;

@Data
public class MessageDTO {
    private final String message;
    private final String username;
    private final Integer timesAsshole;

    public MessageDTO(Message message) {
        this.timesAsshole = message.getAccount().getTimesAsshole();
        this.message = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getMessage()));
        this.username = HtmlUtils.htmlEscape(StringEscapeUtils.unescapeJava(message.getAccount().getUsername()));
    }
}
