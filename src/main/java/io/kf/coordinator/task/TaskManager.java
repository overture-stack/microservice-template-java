package io.kf.coordinator.task;

import io.kf.coordinator.exceptions.TaskException;
import io.kf.coordinator.exceptions.TaskManagerException;
import io.kf.coordinator.exceptions.TaskNotFoundException;
import io.kf.coordinator.model.AuthorizedTaskRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.kf.coordinator.exceptions.TaskManagerException.checkTaskManager;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
public abstract class TaskManager {
  private static final String ETL_MANAGER = "ETL Manager:";

  /**
   * State
   */
  private Map<String, Task> tasks = new HashMap<>();

  public void dispatch(@NonNull AuthorizedTaskRequest request) {
    val taskId = request.getTask_id();
    val releaseId = request.getRelease_id();
    val action = request.getAction();
    val accessToken = request.getAccessToken();

    switch(action) {

      case get_status:
        // No action to take
        break;

      case initialize:
        log.debug("{} Initialize action for {}", ETL_MANAGER, request.getTask_id());
        try{
          val task = registerTask(accessToken, taskId, releaseId);
          task.handleAction(action, accessToken);
        } catch (Throwable t){
          log.error("{} Failed to initialize task request {}: [{}] -> {}",
              ETL_MANAGER, request, t.getClass().getSimpleName(), t.getMessage());
          throw t;
        }
        break;
      default:
        log.debug("{} Action for {}: {}", ETL_MANAGER, request.getTask_id(), action.name());
        checkTaskExists(taskId);
        val existingTask = tasks.get(taskId);
        if (existingTask != null) {
          existingTask.handleAction(action, accessToken);
        }
        break;
    }
  }

  public Task getTask(String taskId) {
    checkTaskExists(taskId);
    return tasks.get(taskId);
  }

  private boolean isTaskExist(String taskId){
    return tasks.containsKey(taskId);
  }

  private void checkTaskExists(String taskId){
    if (!isTaskExist(taskId)){
      throw new TaskNotFoundException(format("The taskId '%s' could not be found", taskId));
    }
  }

  private void addTask(Task task){
    tasks.put(task.getId(), task);
  }

  private Optional<Task> findTask(String taskId){
    return Optional.ofNullable(tasks.get(taskId));
  }

  private void checkTaskDataMatches(Task existingTask, Task requestedTask){
    checkTaskManager(existingTask.getId().equals(requestedTask.getId()),
        "Existing Task id '%s' does not match requested Task id '%s'",
        existingTask.getId(), requestedTask.getId());

    checkTaskManager(existingTask.getRelease().equals(requestedTask.getRelease()),
        "Existing Task release '%s' does not match requested Task release '%s' for Task id '%s'",
        existingTask.getRelease(), requestedTask.getRelease(), existingTask.getId());
  }

  /**
   * If the task does not exist, create it and register it. If it does, ensure the important bits of information in the task request by the user matches what is persisted/stored in the manager, and just return that
   * @throws TaskManagerException if task exists but does not match some of the data stored, or if the newly created task is null
   */
  private Task registerTask(String accessToken, String taskId, String releaseId){
    val existingTaskResult = findTask(taskId);
    val newTask = createTask(accessToken, taskId, releaseId);
    Task task;
    if (existingTaskResult.isPresent()){
      val existingTask = existingTaskResult.get();
      checkTaskDataMatches(existingTask, newTask);
      task = existingTask;
    } else {
      task = newTask;
    }
    checkTaskManager(!isNull(task),
        "Null task for Task id '%s' with release '%s'", taskId, releaseId);
    addTask(task);
    return task;
  }

  protected abstract Task createTask(String accessToken, String taskId, String releaseId) throws TaskException;

}
