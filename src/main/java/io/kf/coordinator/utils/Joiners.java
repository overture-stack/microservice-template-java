package io.kf.coordinator.utils;

import com.google.common.base.Joiner;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Joiners {

  public static final Joiner PATH = Joiner.on("/");
  public static final Joiner UNDERSCORE = Joiner.on("_");
  public static final Joiner COMMA = Joiner.on(",");
  public static final Joiner WHITESPACE = Joiner.on(" ");

}
