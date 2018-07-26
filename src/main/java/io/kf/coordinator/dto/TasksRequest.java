package io.kf.coordinator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kf.coordinator.task.TaskAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasksRequest {

  private TaskAction action;

  private String task_id;

  private String release_id;

  private Set<String> studyIds;

}
