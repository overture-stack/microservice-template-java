package io.kf.coordinator.model.rollcall;

import lombok.Data;

import java.util.List;

@Data
public class AliasCandidate {
  private ConfiguredAlias alias;
  private List<ResolvedIndex> indices;
}
