package io.kf.coordinator.exceptions;

import lombok.NonNull;

import static java.lang.String.format;

public class TaskException extends RuntimeException {

  public TaskException() {
  }

  public TaskException(String s) {
    super(s);
  }

  public TaskException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskException(Throwable cause) {
    super(cause);
  }

  public static void checkTask(boolean expression, @NonNull String formattedMessage, @NonNull Object...objects)
      throws TaskException {
    if (!expression){
      throw new TaskException(format(formattedMessage, objects));
    }
  }

}
