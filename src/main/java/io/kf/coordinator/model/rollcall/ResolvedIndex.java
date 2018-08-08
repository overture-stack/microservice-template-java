package io.kf.coordinator.model.rollcall;

import lombok.Data;

@Data
public class ResolvedIndex {
  private String indexName;
  private String entity;
  private String shardPrefix;
  private String shard;
  private String releasePrefix;
  private String release;
  private boolean valid;
}
