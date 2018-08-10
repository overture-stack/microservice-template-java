package io.kf.coordinator.config;

import io.kf.coordinator.utils.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PublishServiceConfig {

  @Value("${rollcall.auth.enable}")
  private boolean enableRollCallAuth;

  @Bean
  public RestClient rollCallRestClient(){
    return new RestClient(new RestTemplate(), enableRollCallAuth);
  }

}
