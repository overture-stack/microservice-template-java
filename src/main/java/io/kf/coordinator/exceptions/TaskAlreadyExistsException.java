package io.kf.coordinator.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Task ID already exists")
public class TaskAlreadyExistsException extends RuntimeException {

  public TaskAlreadyExistsException() {
  }

  public TaskAlreadyExistsException(String message) {
    super(message);
  }

  public TaskAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  public TaskAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
