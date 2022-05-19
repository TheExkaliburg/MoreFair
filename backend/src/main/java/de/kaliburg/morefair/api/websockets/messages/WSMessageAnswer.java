package de.kaliburg.morefair.api.websockets.messages;

import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class WSMessageAnswer<T> {
    @NonNull
    private T content;
    @NonNull
    private HttpStatus status = HttpStatus.OK;
}
