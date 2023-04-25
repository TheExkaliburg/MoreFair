package de.kaliburg.morefair.exceptions;

import lombok.Data;

/**
 * A Data Transfer Object for Error Messages.
 */
@Data
public class ErrorDto {

  private String message;
  private String cause;

  public ErrorDto(Exception e) {
    this.message = e.getMessage();
    this.cause = e.getCause().getMessage();
  }
}
