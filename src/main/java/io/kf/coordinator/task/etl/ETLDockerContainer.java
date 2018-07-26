package io.kf.coordinator.task.etl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ETLDockerContainer {

  private static final String STUDY_ID_ENV_VAR = "KF_STUDY_ID";
  private static final String RELEASE_ID_ENV_VAR = "KF_RELEASE_ID";
  private static final String CONTAINER_CONF_LOC = "/kf-etl/mnt/conf/kf_etl.conf";
  private static final String CONTAINER_JAR_LOC = "/kf-etl/mnt/lib/kf-portal-etl.jar";
  private final DockerClient docker;
  private String id;

//  final private static String dockerImage = "busybox:latest";
  final private static String[] ports = {};

  private final String dockerImage;
  private final Path etlConfFile;
  private final Path etlJarFile;
  private final boolean useLocal;
  private final String networkId;

  private boolean hasStarted = false;
  private boolean hasFinished = false;

  private String dockerMainCommand = "for i in `seq 1 60`; do sleep 1; echo $i; done";
  private String dockerTriggeredCommand = "echo \"DOCKER RECEIVED RUN COMMAND\"";

  @Autowired
  public ETLDockerContainer(
      @Value("${docker.imageId}") @NonNull String dockerImage,
      @Value("${docker.useLocal}") boolean useLocal,
      @Value("${docker.input.conf}") @NonNull String etlConfFilePath,
      @Value("${docker.input.jar}") @NonNull String etlJarFilePath,
      @Value("${docker.networkId}") @NonNull String networkId
  ) throws InterruptedException, DockerException, DockerCertificateException {
    this.dockerImage = dockerImage;
    this.useLocal = useLocal;
    this.etlConfFile = getConfFile(etlConfFilePath);
    this.etlJarFile = getJarFile(etlJarFilePath);
    this.docker = DefaultDockerClient.fromEnv().build();
    this.networkId = networkId;
    initImage();
  }

  @SneakyThrows
  private void checkImageExists(String dockerImage){
    docker.inspectImage(dockerImage);
  }

  @SneakyThrows
  private void checkNetworkExists(String networkId){
      docker.inspectNetwork(networkId);
  }

  @SneakyThrows
  private static Path getFile(String filename){
    val path = Paths.get(filename);
    checkArgument(exists(path), "The path '%s' does not exist", filename);
    checkArgument(isRegularFile(path.toRealPath()), "The path '%s' is not a regular file", filename);
    return path.toAbsolutePath();
  }

  private static Path getJarFile(String filename){
    checkArgument(filename.endsWith(".jar"), "The filename '%s' is not a .jar file", filename);
    return getFile(filename);
  }

  private static Path getConfFile(String filename){
    checkArgument(filename.endsWith(".conf"), "The filename '%s' is not a .conf file", filename);
    return getFile(filename);
  }

  private void initImage() throws DockerException, InterruptedException {
    if (!useLocal) {
      docker.pull(dockerImage);
    }
  }

  private void checkRunPreconditions(){
    checkNetworkExists(networkId);
    checkImageExists(dockerImage);
  }

  public void startContainer(@NonNull String studyId, @NonNull String releaseId) throws DockerException, InterruptedException {
    // Bind container ports to host ports
    /*
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
    */

    val hostConfig = HostConfig.builder().build();
    // Create container with exposed ports
    val containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(dockerImage)
        .addVolume(getVolumeTerm(etlConfFile, CONTAINER_CONF_LOC))
        .addVolume(getVolumeTerm(etlJarFile, CONTAINER_JAR_LOC))
        .env(getEnvTerm(STUDY_ID_ENV_VAR, studyId))
        .env(getEnvTerm(RELEASE_ID_ENV_VAR, releaseId))
        .build();

    checkRunPreconditions();
    val creation = docker.createContainer(containerConfig);
    this.id = creation.id();
    docker.connectToNetwork(id, networkId);
  }

  private static String getEnvTerm(String envVar, String value){
    checkArgument(isNotBlank(value), "The env var '%s' cannot have a blank value ", envVar );
    checkArgument(!isPaddedWithWhitespace(value), "The value '%s' for env var '%s' cannot be epadded with whitespaces",value, envVar );
    return generalTerm(envVar, value);
  }

  private static boolean isPaddedWithWhitespace(String value){
    return value.matches("^\\s+|\\s+$");
  }

  private static String getVolumeTerm(Path hostFile, String containerFilePath){
    return generalTerm(hostFile.toString(), containerFilePath);
  }

  private static String generalTerm(String left, String right){
    return format("%s:%s", left, right);
  }

  public void runETL() throws DockerException, InterruptedException {
    checkRunPreconditions();
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

  public boolean isComplete() throws DockerException, InterruptedException {
    if(hasStarted && !hasFinished) {
      hasFinished = !isRunning();
    }
    return hasFinished;
  }

  private boolean isRunning() throws DockerException, InterruptedException {
    val info = docker.inspectContainer(this.id);
    return info.state().running();
  }

  public boolean finishedWithErrors() throws DockerException, InterruptedException {
    checkState(isComplete(), "The container '%s' has not started or has not finished", id);
    val info = docker.inspectContainer(id);
    return info.state().exitCode() > 0;
  }


}
