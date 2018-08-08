package io.kf.coordinator.model.rollcall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollCallRequest {
  private String alias;
  private String release;
  private Set<String> shards;
}
