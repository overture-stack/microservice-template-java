package io.kf.coordinator.task.etl;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import io.kf.coordinator.utils.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.kf.coordinator.exceptions.TaskException.checkTask;
import static io.kf.coordinator.utils.Files.getAbsoluteFile;
import static io.kf.coordinator.utils.Joiners.WHITESPACE;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

// TODO: check that the ES index name doesnt already exist.
@Slf4j
public class ETLDockerContainer {

  private static final String STUDY_ID_ENV_VAR = "KF_STUDY_ID";
  private static final String RELEASE_ID_ENV_VAR = "KF_RELEASE_ID";
  private static final String CONTAINER_CONF_LOC = "/kf-etl/conf/kf_etl.conf";
  private static final String CONTAINER_JAR_LOC = "/kf-etl/mnt/lib/kf-portal-etl.jar";

  /**
   * Config
   */
  private final String dockerImage;
  private final boolean useLocal;
  private final Path etlConfFile;
  private final String networkId;

  /**
   * Dependencies
   */
  private final DockerClient docker;

  /**
   * State
   */
  private String id;
  @Getter private String displayName;
  private boolean createdContainer = false;
  private boolean hasStarted = false;
  private boolean hasFinished = false;

  public ETLDockerContainer(
      @NonNull String dockerImage,
      boolean useLocal,
      @NonNull String etlConfFilePath,
      @NonNull String networkId,
      @NonNull DockerClient docker
  ) throws InterruptedException, DockerException {
    this.dockerImage = dockerImage;
    this.useLocal = useLocal;
    this.etlConfFile = getConfFile(etlConfFilePath);
    this.docker = docker;
    this.networkId = networkId;
    initImage();
    log.info("Instantiated ETL Container controller with image '{}' and network '{}'", dockerImage, networkId);
  }

  public void createContainer(@NonNull Set<String> studyIds, @NonNull String releaseId)
      throws DockerException, InterruptedException {
    log.info("Creating ETLContainer with releaseId '{}'", releaseId);
    val hostConfig = HostConfig.builder()
        .appendBinds(getMountBind(etlConfFile, CONTAINER_CONF_LOC))
        .build();

    // Create container with exposed ports
    val containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(dockerImage)
        .env(
            getEnvTerm(RELEASE_ID_ENV_VAR, releaseId),
            getEnvTerm(STUDY_ID_ENV_VAR, WHITESPACE.join(studyIds))
        )
        .build();

    checkRunPreconditions();
    val creation = docker.createContainer(containerConfig);

    id = creation.id();
    displayName = createDisplayName(id);

    docker.connectToNetwork(id, networkId);
    log.info("Created {} with release '{}'", getDisplayName(), releaseId);
    createdContainer = true;
  }

  public void runETL() throws DockerException, InterruptedException {
    checkContainerCreated();
    log.info("Starting {}", getDisplayName());
    checkRunPreconditions();
    docker.startContainer(id);

    hasStarted = true;
    log.info("{} started", getDisplayName());
  }

  public boolean isStarted() {
    return hasStarted;
  }

  public boolean isComplete() throws DockerException, InterruptedException {
    checkContainerCreated();
    if (isStarted() && !hasFinished) {
      hasFinished = !isRunning();
      if (hasFinished) {
        val logOutput = getLogs();
        if (finishedWithErrors()) {
          log.error("{} has completed with ERRORS. Logs: \n{}", getDisplayName(), logOutput);
        } else {
          log.info("{} has SUCCESSFULLY completed. Logs: \n{}", getDisplayName(), logOutput);
        }
      }
    }
    return hasFinished;
  }

  public boolean finishedWithErrors() throws DockerException, InterruptedException {
    checkContainerCreated();
    checkState(isStarted(), "The {} has not started", getDisplayName());
    checkState(isComplete(), "The {} has not finished", getDisplayName());
    val info = docker.inspectContainer(id);
    return info.state().exitCode() > 0;
  }

  /**
   * Try to kill the docker if it exists and is running
   *
   * @throws DockerException
   * @throws InterruptedException
   */
  public void kill() throws DockerException, InterruptedException {
    if (!isContainerExists()) {
      log.info("{} does not exist. Aborting kill", getDisplayName());
    } else if (!isRunning()) {
      log.info("{} exists and was not running. Aborting kill", getDisplayName());
    } else {
      log.info("{} exists and still running. Killing it", getDisplayName());
      docker.killContainer(id);
      checkTask(!isRunning(), "Killing of '{}' failed", getDisplayName());
    }
  }

  /**
   * Try to remove the container if it exists
   *
   * @throws DockerException
   * @throws InterruptedException
   */
  public void remove() throws DockerException, InterruptedException {
    if (isContainerExists()) {
      log.info("{} exists. Removing it", getDisplayName());
      docker.removeContainer(id);
      checkTask(!isContainerExists(), "Removing of '{}' failed", getDisplayName());
    } else {
      log.info("{} does not exist. Aborting remove", getDisplayName());
    }
  }

  /**
   * Check if the container exists
   */
  public boolean isContainerExists() {
    try {
      checkContainerCreated();
      docker.inspectContainer(id);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  /**
   * Try to cancel and remove the container
   *
   * @throws DockerException
   * @throws InterruptedException
   */
  public void killAndRemove() throws DockerException, InterruptedException {
    log.info("Cancelling and removing {}", getDisplayName());
    kill();
    val logs = isContainerExists() ? getLogs() : format("%s does not exist, could not retrieve logs", getDisplayName());
    remove();
    log.info("Cancelled and removed {}: Logs: {}", getDisplayName(), logs);
  }

  private static String createDisplayName(String id) {
    return "ETLContainer[" + id.substring(0, 13) + "]";
  }

  private void checkContainerExists() {
    checkState(isContainerExists(), "The {} does not exist", getDisplayName());
  }

  private void checkContainerCreated() {
    checkState(createdContainer, "The ETLContainer has not been created");
  }

  private boolean isRunning() throws DockerException, InterruptedException {
    val info = docker.inspectContainer(this.id);
    return info.state().running();
  }

  private void checkImageExists(String dockerImage) throws DockerException, InterruptedException {
    docker.inspectImage(dockerImage);
  }

  private void checkNetworkExists(String networkId) throws DockerException, InterruptedException {
    docker.inspectNetwork(networkId);
  }

  private void initImage() throws DockerException, InterruptedException {
    if (!useLocal) {
      docker.pull(dockerImage);
    }
  }

  private void checkRunPreconditions() throws DockerException, InterruptedException {
    checkNetworkExists(networkId);
    checkImageExists(dockerImage);
  }

  private String getLogs() throws DockerException, InterruptedException {
    checkContainerCreated();
    try (val stream = docker.logs(id, LogsParam.stdout(), LogsParam.stderr())) {
      return stream.readFully();
    }
  }

  private static String getEnvTerm(String envVar, String value) {
    checkArgument(isNotBlank(value), "The env var '%s' cannot have a blank value ", envVar);
    checkArgument(!Strings.isPaddedWithWhitespace(value),
        "The value '%s' for env var '%s' cannot be padded with whitespaces", value, envVar);
    return format("%s=\"%s\"", envVar, value);
  }

  private static Bind getMountBind(Path hostFile, String containerFilePath) {
    return Bind.from(hostFile.toString())
        .to(containerFilePath)
        .readOnly(true)
        .build();
  }

  private static Path getJarFile(String filename) {
    checkArgument(filename.endsWith(".jar"), "The filename '%s' is not a .jar file", filename);
    return getAbsoluteFile(filename);
  }

  private static Path getConfFile(String filename) {
    checkArgument(filename.endsWith(".conf"), "The filename '%s' is not a .conf file", filename);
    return getAbsoluteFile(filename);
  }

}
