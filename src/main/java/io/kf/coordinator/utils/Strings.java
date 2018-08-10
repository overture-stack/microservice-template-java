package io.kf.coordinator.utils;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Strings {

  private static final String NEWLINE = "\n";

  public static boolean isPaddedWithWhitespace(String value){
    return value.matches("^\\s+|\\s+$");
  }

  public static String extractStackTrace(@NonNull Throwable t){
    return stream(t.getStackTrace()).map(StackTraceElement::toString).collect(joining(NEWLINE));
  }

}
