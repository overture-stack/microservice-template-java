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
      .cmd("sh", "-c", "while :; do sleep 1; done")
      .build();

    final ContainerCreation creation = docker.createContainer(containerConfig);
    this.id = creation.id();

    // Inspect container
    final ContainerInfo info = docker.inspectContainer(this.id);

    // Start container
    docker.startContainer(id);

  }

  public void runETL() throws DockerException, InterruptedException {
    // Exec command inside running container with attached STDOUT and STDERR
    final String[] command = {"sh", "-c", "ls"};
    final ExecCreation execCreation = docker.execCreate(
      this.id, command, DockerClient.ExecCreateParam.attachStdout(),
      DockerClient.ExecCreateParam.attachStderr());
    final LogStream output = docker.execStart(execCreation.id());
    final String execOutput = output.readFully();

    this.hasStarted = true;

  }

  public boolean isComplete() {

    return this.hasStarted && this.hasFinished;
  }


}
