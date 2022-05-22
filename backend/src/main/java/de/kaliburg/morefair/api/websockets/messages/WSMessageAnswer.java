package de.kaliburg.morefair.api.websockets.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
