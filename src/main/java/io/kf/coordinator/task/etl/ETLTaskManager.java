package io.kf.coordinator.task.etl;

import io.kf.coordinator.config.ETLDockerContainerConfig;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.TaskManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Component
public class ETLTaskManager extends TaskManager {

  //TODO: must manage studyId:ReleaseId pairs. if another task is created with the same releaseId and one of the previoulsy submitted studyIds, it should error out
  @Autowired
  private ETLDockerContainerConfig factory;

  @Override
  protected Task createTask(@NonNull String taskId, @NonNull String releaseId, @NonNull Set<String> studyIds) {
    checkArgument(!studyIds.isEmpty(), "Must have at least one studyId");

    try {
      return new ETLTask(factory.createETLDockerContainer(), taskId, releaseId, studyIds);

    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

}

