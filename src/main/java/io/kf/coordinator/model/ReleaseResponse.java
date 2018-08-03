package io.kf.coordinator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseResponse {

	@JsonProperty("kf_id")
	private String kfId;

	private String name;
	private String description;
	private String state;
	private Set<String> studies;

	@JsonProperty("created_at")
  private String createdAt;

  private String author;

}
