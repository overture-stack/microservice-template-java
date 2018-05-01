package io.kf.coordinator.task.etl;

import io.kf.coordinator.task.Task;

public class ETLTask extends Task{

  public ETLTask(String id, String release) throws Exception {
    super(id, release);
  }

  @Override
  public Void initialize() {
    return null;
  }

  @Override
  public Void run() {
    return null;
  }

  @Override
  public Void publish() {
    return null;
  }
}
