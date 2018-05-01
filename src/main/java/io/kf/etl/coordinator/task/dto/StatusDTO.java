package io.kf.etl.coordinator.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusDTO {

  @Builder.Default private String name = "KF ETL Task Runner!";
                   private String message;
  @Builder.Default private String version = "0.0.1";
}
