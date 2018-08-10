package io.kf.coordinator.model;

import io.kf.coordinator.task.TaskAction;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class AuthorizedTaskRequest {
  @NonNull private TaskAction action;

  @NonNull private String task_id;

  private String release_id;

  private String accessToken;
}
