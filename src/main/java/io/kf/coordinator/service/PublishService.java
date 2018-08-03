package io.kf.coordinator.service;

import com.google.common.collect.ImmutableList;
import io.kf.coordinator.model.PublishReleaseError;
import io.kf.coordinator.model.rollcall.AliasCandidate;
import io.kf.coordinator.model.rollcall.ConfiguredAlias;
import io.kf.coordinator.model.rollcall.RollCallRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static io.kf.coordinator.utils.Collectors.toImmutableList;
import static io.kf.coordinator.utils.Collectors.toImmutableSet;
import static io.kf.coordinator.utils.Joiners.UNDERSCORE;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class PublishService {

  private static final String PADDED_PIPE = " | ";
  private final RollCallClient client;

  @Autowired
  public PublishService(@NonNull RollCallClient client){
    this.client = client;
  }

  public void publishRelease(@NonNull String release, @NonNull Set<String> studies){
    val candidates = client.getAliasCandidates();
    val errors = validateCandidates(candidates, release, studies);
    checkState(errors.isEmpty(), buildErrorMessage(errors));
    buildRequests(extractAliases(candidates), release, studies)
        .forEach(client::release);
  }

  List<PublishReleaseError> validateCandidates(List<AliasCandidate> aliasCandidates, String expectedRelease,
      Set<String> expectedStudies){
    val errors = ImmutableList.<PublishReleaseError>builder();
    for (val candidate : aliasCandidates){
      val aliasName = candidate.getConfiguredAlias().getAlias();
      val indices = candidate.getIndices();
      val foundRelease = indices.stream()
          .map(x -> UNDERSCORE.join(x.getReleasePrefix(), x.getRelease()))
          .anyMatch(x -> x.equals(expectedRelease));

      val actualStudies = indices.stream()
          .map(x -> UNDERSCORE.join(x.getShardPrefix(), x.getShard()))
          .collect(toImmutableSet());

      val foundAllStudies = actualStudies.containsAll(expectedStudies);

      if (!foundRelease || !foundAllStudies){
        val error = PublishReleaseError.builder()
            .alias(aliasName)
            .expectedRelease(expectedRelease)
            .missingRelease(foundRelease)
            .expectedStudies(expectedStudies)
            .missingStudies(expectedStudies.stream()
                .filter(x -> !actualStudies.contains(x))
                .collect(toImmutableSet()))
            .build();
        errors.add(error);
      }
    }
    return errors.build();
  }

  private List<RollCallRequest> buildRequests(Set<String> aliases, String release, Set<String> studies){
    return aliases.stream()
        .map(a -> RollCallRequest.builder()
            .alias(a)
            .release(release)
            .shards(studies)
            .build())
        .collect(toImmutableList());
  }

  private static Set<String> extractAliases(List<AliasCandidate> candidates){
    return candidates.stream()
        .map(AliasCandidate::getConfiguredAlias)
        .map(ConfiguredAlias::getAlias)
        .collect(toImmutableSet());
  }

  private static String buildErrorMessage(List<PublishReleaseError> errors){
    return errors.stream()
        .map(PublishReleaseError::toString)
        .collect(joining(PADDED_PIPE));
  }

}
