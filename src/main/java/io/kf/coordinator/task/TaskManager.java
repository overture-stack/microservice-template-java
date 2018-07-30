package io.kf.coordinator.task;

import io.kf.coordinator.dto.TasksRequest;
import io.kf.coordinator.exceptions.TaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class TaskManager {

  private Map<String, Task> tasks = new HashMap<>();

  public void dispatch(TasksRequest request) {
    val taskId = request.getTask_id();
    val releaseId = request.getRelease_id();
    val action = request.getAction();

    switch(action) {

      case status:
        // No action to take
        break;

      case initialize:
        log.debug("ETL Manager: Initialize action for " + request.getTask_id());
        try{
          val newTask = createTask(taskId, releaseId);
          if (newTask != null) {
            tasks.put(taskId, newTask);
            newTask.handleAction(action);
          }
        } catch (Throwable t){
          log.error("ETL Manager: Failed to initialize task request: {}", request);
        }
        break;
      default:
        log.debug("ETL Manager: Action for " + request.getTask_id() + ": " + action.name());
        val existingTask = tasks.get(taskId);
        if (existingTask != null) {
          existingTask.handleAction(action);
        }
        break;
    }
  }

  public Task getTask(String taskId) {
    return tasks.get(taskId);
  }

  protected abstract Task createTask(String taskId, String releaseId) throws TaskException;

}
