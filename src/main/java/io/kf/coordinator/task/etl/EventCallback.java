package io.kf.coordinator.task.etl;

public interface EventCallback {

  void onRun() throws Throwable;

  void onError(Throwable t);
}
