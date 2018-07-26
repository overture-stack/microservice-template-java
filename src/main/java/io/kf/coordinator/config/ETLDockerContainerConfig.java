package io.kf.coordinator.config;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import io.kf.coordinator.task.etl.ETLDockerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ETLDockerContainerConfig {

  @Value("${docker.image.id}")
  private String dockerImage;

  @Value("${docker.image.useLocal}")
  private boolean useLocal;

  @Value("${docker.input.conf}")
  private String etlConfFilePath;

  @Value("${docker.input.jar}")
  private String etlJarFilePath;

  @Value("${docker.network.id}")
  private String networkId;

  public ETLDockerContainer createETLDockerContainer()
      throws InterruptedException, DockerException, DockerCertificateException {
    return new ETLDockerContainer(dockerImage, useLocal, etlConfFilePath, etlJarFilePath, networkId);
  }

}
