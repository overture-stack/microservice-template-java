package io.kf.coordinator.service;

import com.google.common.collect.ImmutableList;
import io.kf.coordinator.model.PublishReleaseError;
import io.kf.coordinator.model.rollcall.AliasCandidate;
import io.kf.coordinator.model.rollcall.ConfiguredAlias;
import io.kf.coordinator.model.rollcall.RollCallRequest;
import io.kf.coordinator.utils.RestClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static io.kf.coordinator.utils.RestClient.tryNotFoundRequest;
import static io.kf.coordinator.utils.Collectors.toImmutableList;
import static io.kf.coordinator.utils.Collectors.toImmutableSet;
import static io.kf.coordinator.utils.Joiners.PATH;
import static io.kf.coordinator.utils.Joiners.UNDERSCORE;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class PublishService {

  private static final String PADDED_PIPE = " | ";
  private static final String RELEASE_ENDPOINT = "aliases/release";
  private static final String CANDIDATE_ENDPOINT = "aliases/candidates";

  private final String serverUrl;
  private final RestClient rest;

  @Autowired
  public PublishService(
      @Value("${rollcall.url}")
      @NonNull String serverUrl,
      @NonNull RestClient rollCallRestClient) {
    this.serverUrl = serverUrl;
    this.rest = rollCallRestClient;
  }

  public void publishRelease(@NonNull String accessToken, @NonNull String release, @NonNull Set<String> studies){
    val candidates = getAliasCandidates(accessToken);
    val errors = validateCandidates(candidates, release, studies);
    checkState(errors.isEmpty(), buildErrorMessage(errors));
    buildRequests(extractAliases(candidates), release, studies)
        .forEach(x -> release(accessToken, x));
  }

  private List<AliasCandidate> getAliasCandidates(String accessToken){
    return rest.gets(accessToken, getCandidatesUrl(), AliasCandidate.class).getBody();
  }

  private Optional<Boolean> release(String accessToken, @NonNull RollCallRequest request) {
    return tryNotFoundRequest(() -> rest.post(accessToken, getReleaseUrl(), request, Boolean.class))
        .map(HttpEntity::getBody);
  }

  private String getReleaseUrl(){
    return PATH.join(serverUrl, RELEASE_ENDPOINT);
  }

  private String getCandidatesUrl(){
    return PATH.join(serverUrl, CANDIDATE_ENDPOINT);
  }

  List<PublishReleaseError> validateCandidates(List<AliasCandidate> aliasCandidates, String expectedRelease,
      Set<String> expectedStudies){
    val lcExpectedRelease = expectedRelease.toLowerCase();
    val lcExpectedStudies = expectedStudies.stream().map(String::toLowerCase).collect(toImmutableSet());
    val errors = ImmutableList.<PublishReleaseError>builder();
    for (val candidate : aliasCandidates){
      val aliasName = candidate.getAlias().getAlias();
      val indices = candidate.getIndices();
      val foundRelease = indices.stream()
          .map(x -> UNDERSCORE.join(x.getReleasePrefix(), x.getRelease()))
          .map(String::toLowerCase)
          .anyMatch(x -> x.equals(lcExpectedRelease));

      val actualStudies = indices.stream()
          .map(x -> UNDERSCORE.join(x.getShardPrefix(), x.getShard()))
          .map(String::toLowerCase)
          .collect(toImmutableSet());

      val foundAllStudies = actualStudies.containsAll(lcExpectedStudies);

      if (!foundRelease || !foundAllStudies){
        val error = PublishReleaseError.builder()
            .alias(aliasName)
            .expectedRelease(expectedRelease)
            .missingRelease(foundRelease)
            .expectedStudies(lcExpectedStudies)
            .missingStudies(lcExpectedStudies.stream()
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
        .map(AliasCandidate::getAlias)
        .map(ConfiguredAlias::getAlias)
        .collect(toImmutableSet());
  }

  private static String buildErrorMessage(List<PublishReleaseError> errors){
    return errors.stream()
        .map(PublishReleaseError::toString)
        .collect(joining(PADDED_PIPE));
  }

}
