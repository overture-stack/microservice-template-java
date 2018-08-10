package io.kf.coordinator.config;

import io.kf.coordinator.utils.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ReleaseServiceConfig {

  @Value("${release-coordinator.auth.enable}")
  private boolean enableReleaseCoordinatorAuth;

  @Bean
  public RestClient releaseCoordinatorRestClient(){
    return new RestClient(new RestTemplate(), enableReleaseCoordinatorAuth);
  }

}
