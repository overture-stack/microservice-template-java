package io.kf.coordinator.utils;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Files {

  @SneakyThrows
  public static Path getFile(String filename) {
    val path = Paths.get(filename);
    checkArgument(exists(path), "The path '%s' does not exist", filename);
    checkArgument(isRegularFile(path.toRealPath()), "The path '%s' is not a regular file", filename);
    return path;
  }

  public static Path getAbsoluteFile(String filename){
    return getFile(filename).toAbsolutePath();
  }

}
