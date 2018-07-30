package io.kf.coordinator.utils;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Strings {

  public static boolean isPaddedWithWhitespace(String value){
    return value.matches("^\\s+|\\s+$");
  }

}
