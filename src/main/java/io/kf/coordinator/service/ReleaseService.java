package io.kf.coordinator.service;

import io.kf.coordinator.model.ReleaseResponse;
import io.kf.coordinator.utils.RestClient;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.kf.coordinator.utils.RestClient.tryNotFoundRequest;
import static io.kf.coordinator.utils.Joiners.PATH;

@Service
public class ReleaseService {

  private static final String RELEASES = "releases";
  private final String serverUrl;
  private final RestClient rest;

  @Autowired
  public ReleaseService(
      @Value("${release-coordinator.url}")
      @NonNull String serverUrl,
      @NonNull RestClient releaseCoordinatorRestClient) {
    this.serverUrl = serverUrl;
    this.rest = releaseCoordinatorRestClient;
  }

  public Optional<Set<String>> getStudies(String accessToken, @NonNull String releaseId){
    return getRelease(accessToken, releaseId).map(ReleaseResponse::getStudies);
  }

  private Optional<ReleaseResponse> getRelease(String accessToken, @NonNull String releaseId){
    return tryNotFoundRequest(() -> rest.get(accessToken, getReleaseUrl(releaseId), ReleaseResponse.class))
        .map(HttpEntity::getBody);
  }

  private String getReleaseUrl(String releaseId){
    return PATH.join(serverUrl, RELEASES, releaseId);
  }

}
