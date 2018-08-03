package io.kf.coordinator.service;

import io.kf.coordinator.model.rollcall.AliasCandidate;
import io.kf.coordinator.model.rollcall.RollCallRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static io.kf.coordinator.utils.Joiners.PATH;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Service
public class RollCallClient {

  private static final String RELEASE_ENDPOINT = "aliases/release";
  private static final String CANDIDATE_ENDPOINT = "aliases/candidates";

  private final String serverUrl;
  private final RestTemplate rest;

  @Autowired
  public RollCallClient(
      @Value("${rollcall.url}")
      @NonNull String serverUrl,
      @NonNull RestTemplate rollCallTemplate) {
    this.serverUrl = serverUrl;
    this.rest = rollCallTemplate;
  }

  public List<AliasCandidate> getAliasCandidates(){
    return rest.exchange(getCandidatesUrl(), HttpMethod.GET,
        null, new ParameterizedTypeReference<List<AliasCandidate>>(){}).getBody();
  }

  public Optional<Boolean> release(@NonNull RollCallRequest request) {
    Boolean response;
    try{
      response = rest.postForEntity(getReleaseUrl(), request, Boolean.class).getBody();
    } catch (HttpClientErrorException e){
      if (e.getRawStatusCode() == NOT_FOUND.getStatusCode()){
        return Optional.empty();
      }
      throw e;
    }
    return Optional.of(response);
  }

  private String getReleaseUrl(){
    return PATH.join(serverUrl, RELEASE_ENDPOINT);
  }

  private String getCandidatesUrl(){
    return PATH.join(serverUrl, CANDIDATE_ENDPOINT);
  }

}
