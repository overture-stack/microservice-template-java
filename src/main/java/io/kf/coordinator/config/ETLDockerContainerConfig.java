package io.kf.coordinator.config;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ETLDockerContainerConfig {

  @Value("${docker.image.id}")
  private String dockerImage;

  @Value("${docker.image.useLocal}")
  private boolean useLocal;

  @Value("${docker.input.conf}")
  private String etlConfFilePath;

  @Value("${docker.network.id}")
  private String networkId;

  @Bean
  @SneakyThrows
  public DockerClient docker(){
    return DefaultDockerClient.fromEnv().build();
  }

}
