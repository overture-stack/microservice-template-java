package io.kf.coordinator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatusDTO {

  @Builder.Default private String name = "KF ETL Task Runner!";
                   private String message;
  @Builder.Default private String version = "0.0.1";

  @JsonProperty("queued_tasks")
  private List<TasksDTO> queuedTasks;

  @JsonProperty("queue_size")
  private int queueSize;

}
