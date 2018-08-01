package io.kf.coordinator.exceptions;

import lombok.NonNull;

import static java.lang.String.format;

public class TaskManagerException extends RuntimeException {

  public TaskManagerException() {
  }

  public TaskManagerException(String s) {
    super(s);
  }

  public TaskManagerException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskManagerException(Throwable cause) {
    super(cause);
  }

  public static void checkTaskManager(boolean expression, @NonNull String formattedMessage, @NonNull Object...objects)
      throws TaskManagerException {
    if (!expression){
      throw new TaskManagerException(format(formattedMessage, objects));
    }
  }

}
