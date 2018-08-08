package io.kf.coordinator.model.rollcall;

import lombok.Data;

@Data
public class ConfiguredAlias {
  private String alias;
  private String entity;
  private String type;
}
