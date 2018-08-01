package io.kf.coordinator.exceptions;

public class IllegalEventRequestException extends RuntimeException {

  public IllegalEventRequestException() {
  }

  public IllegalEventRequestException(String message) {
    super(message);
  }

  public IllegalEventRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalEventRequestException(Throwable cause) {
    super(cause);
  }

  public IllegalEventRequestException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
