package io.kf.coordinator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kf.coordinator.task.TaskAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasksRequest {

  private TaskAction action;

  private String task_id;

  private String release_id;

}
