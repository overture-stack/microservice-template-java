package io.kf.coordinator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import static io.kf.coordinator.utils.Joiners.COMMA;
import static java.lang.String.format;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublishReleaseError {
  private String alias;
  private Set<String> expectedStudies;
  private Set<String> missingStudies;
  private String expectedRelease;
  private boolean missingRelease;

  public boolean isMissingStudies(){
    return !missingStudies.isEmpty();
  }

  public String toString(){
    String out;
    if (isMissingRelease()){
      out = format("Alias '%s' is missing release '%s'", alias, expectedRelease);
    } else if (isMissingStudies()){
      out = format("Alias '%s' for release '%s' is missing studies '%s'",
          alias, expectedRelease, COMMA.join(missingStudies));
    } else {
      out = format("No errors for alias '%s' with release '%s' and studies '%s'",
          alias, expectedRelease, COMMA.join(missingStudies));
    }
    return out;
  }

}
