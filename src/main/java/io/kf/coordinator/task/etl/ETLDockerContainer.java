package io.kf.coordinator.task.etl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ETLDockerContainer {

  public DockerClient docker;
  private String id;

  final private static String dockerImage = "busybox:latest";
  final private static String[] ports = {};

  private boolean hasStarted = false;
  private boolean hasFinished = false;

  private String dockerMainCommand = "for i in `seq 1 60`; do sleep 1; echo $i; done";
  private String dockerTriggeredCommand = "echo \"DOCKER RECEIVED RUN COMMAND\"";

  public ETLDockerContainer() throws DockerCertificateException, DockerException, InterruptedException {
    docker = DefaultDockerClient.fromEnv().build();

    docker.pull(dockerImage);
  }

  public void startContainer() throws DockerException, InterruptedException {
    // Bind container ports to host ports
    final Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : ports) {
      List<PortBinding> hostPorts = new ArrayList<>();
      hostPorts.add(PortBinding.of("0.0.0.0", "11"+port));
      portBindings.put(port, hostPorts);
    }

    // Bind container port 443 to an automatically allocated available host port.
    List<PortBinding> randomPort = new ArrayList<>();
    randomPort.add(PortBinding.randomPort("0.0.0.0"));
    portBindings.put("443", randomPort);

    final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

    // Create container with exposed ports
    final ContainerConfig containerConfig = ContainerConfig.builder()
      .hostConfig(hostConfig)
      .image(dockerImage).exposedPorts(ports)
      .cmd("sh", "-c", dockerMainCommand)
      .build();

    final ContainerCreation creation = docker.createContainer(containerConfig);
    this.id = creation.id();





  }

  public void runETL() throws DockerException, InterruptedException {
    docker.startContainer(id);
    // Start container

    // Exec command inside running container with attached STDOUT and STDERR
//    final String[] command = {"sh", "-c", dockerTriggeredCommand};
//    final ExecCreation execCreation = docker.execCreate(
//      this.id, command, DockerClient.ExecCreateParam.attachStdout(),
//      DockerClient.ExecCreateParam.attachStderr());
//    final LogStream output = docker.execStart(execCreation.id());
//    final String execOutput = output.readFully();

    this.hasStarted = true;
  }

  public boolean isComplete() {

    if(this.hasStarted && !this.hasFinished) {
      this.hasFinished = this.checkFinished();
    }

    return this.hasFinished;
  }

  private boolean checkFinished() {
    // Inspect container
    try {

      final ContainerInfo info = docker.inspectContainer(this.id);
      return !info.state().running();

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (DockerException e) {
      e.printStackTrace();
    }
    return false;

  }


}
