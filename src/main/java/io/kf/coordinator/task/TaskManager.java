package io.kf.coordinator.task;

import io.kf.coordinator.dto.TasksRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class TaskManager {

  private Map<String, Task> tasks;

  public TaskManager() {
    tasks = new HashMap<>();
  }

  public void dispatch(TasksRequest request) {
    val taskId = request.getTask_id();
    val releaseId = request.getRelease_id();
    val action = request.getAction();
    val studyIds = request.getStudyIds();
    switch(action) {

      case status:
        // No action to take
        break;

      case initialize:
        log.debug("Initialize action for " + request.getTask_id());
        val newTask = createTask(taskId, releaseId, studyIds);
        if (newTask != null) {
          tasks.put(taskId, newTask);

          newTask.handleAction(action);
        }
        break;

      default:
        log.debug("Action for " + request.getTask_id() + ": " + action.name());
        Task existingTask = tasks.get(taskId);
        if (existingTask != null) { existingTask.handleAction(action); }
        break;
    }
  }

  public Task getTask(String taskId) {
    return tasks.get(taskId);
  }

  protected abstract Task createTask(String taskId, String releaseId, Set<String> studyIds);

}
