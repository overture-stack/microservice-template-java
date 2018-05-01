package io.kf.etl.coordinator.task;

import io.kf.etl.coordinator.task.dto.TasksRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TaskManager {

  private Map<String, Task> tasks;

  public TaskManager() {
    tasks = new HashMap<String, Task>();
  }

  public void dispatch(TasksRequest request) {
    String taskId = request.getTask_id();
    String releaseId = request.getRelease_id();
    TaskAction action = request.getAction();
    switch(action) {

      case status:
        // No action to take
        break;

      case initialize:
        log.debug("Initialize action for " + request.getTask_id());
        initializeTask(taskId, releaseId);
        break;

      default:
        log.debug("Action for " + request.getTask_id() + ": " + action.name());
        Task task = tasks.get(taskId);
        if (task != null) { task.handleAction(action); }
        break;
    }
  }

  public Task getTask(String taskId) {
    return tasks.get(taskId);
  }

  private void initializeTask(String taskId, String releaseId) {
    Task task;
    try {
      task = new Task(taskId, releaseId);
      tasks.put(taskId, task);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}