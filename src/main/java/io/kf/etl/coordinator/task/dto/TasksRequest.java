package io.kf.etl.coordinator.task.dto;

import io.kf.etl.coordinator.task.TaskAction;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TasksRequest {

  @NonNull
  private TaskAction action;
  @NonNull
  private String task_id;
  // Optional
  private String release_id;
}
