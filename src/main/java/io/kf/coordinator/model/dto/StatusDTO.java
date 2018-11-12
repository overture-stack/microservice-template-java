package io.kf.coordinator.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusDTO {

  @Builder.Default private String name = "KF ETL Task Runner!";
                   private String message;
  @Builder.Default private String version = "1.0.1";
}

