package de.kaliburg.morefair.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidArgumentsException extends RuntimeException {

  public InvalidArgumentsException(String msg) {
    super(msg);
  }
}
