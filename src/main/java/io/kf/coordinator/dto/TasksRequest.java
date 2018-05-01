package io.kf.coordinator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kf.coordinator.task.TaskAction;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasksRequest {

  @NonNull
  private TaskAction action;
  @NonNull
  private String task_id;
  // Optional
  private String release_id;
}
