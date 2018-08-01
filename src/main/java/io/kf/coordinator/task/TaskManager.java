package io.kf.coordinator.task;

import com.google.common.collect.ImmutableList;
import io.kf.coordinator.dto.TasksDTO;
import io.kf.coordinator.dto.TasksRequest;
import io.kf.coordinator.exceptions.TaskAlreadyExistsException;
import io.kf.coordinator.exceptions.TaskException;
import io.kf.coordinator.exceptions.TaskNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.google.common.collect.Queues.newLinkedBlockingQueue;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public abstract class TaskManager {

  /**
   * Config
   */
  @Getter private final int maxQueueSize;

  /**
   * State
   */
  private Map<String, Task> tasks = new HashMap<>();
  private final Queue<String> runningTaskQueue;

  public TaskManager(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    this.runningTaskQueue = maxQueueSize < 1 ?
        newLinkedBlockingQueue() : newLinkedBlockingQueue(maxQueueSize);
  }

  private boolean isQueueFull(){
    return runningTaskQueue.size() >= maxQueueSize;
  }


  public List<TasksDTO> getQueuedTasks(){
    return ImmutableList.copyOf(runningTaskQueue.stream()
        .map(x -> tasks.get(x))
        .map(x -> TasksDTO.builder()
            .release_id(x.getRelease())
            .state(x.getState())
            .task_id(x.getId())
            .progress(x.getProgress())
            .build())
        .collect(toList()));
  }

  private void checkTaskExists(String taskId){
    if (!tasks.containsKey(taskId)){
      throw new TaskNotFoundException(format("The taskId '%s' could not be found", taskId));
    }
  }

  private void checkUniqueTask(String taskId){
    if (tasks.containsKey(taskId)){
      throw new TaskAlreadyExistsException(format("The taskId '%s' already exists: %s", taskId, tasks.get(taskId)));
    }
  }

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
        checkUniqueTask(taskId);
        try{
          val newTask = createTask(taskId, releaseId);
          if (newTask != null) {
//            checkTaskManager(!isQueueFull(),
//                  "The task queue is full with a max size of %s", maxQueueSize);
//            runningTaskQueue.add(newTask.getId());
            tasks.put(taskId, newTask);
            newTask.handleAction(action);
          }
        } catch (Throwable t){
          log.error("ETL Manager: Failed to initialize task request {}: {}", request, t);
        }
        break;
      default:
        log.debug("ETL Manager: Action for " + request.getTask_id() + ": " + action.name());
        checkTaskExists(taskId);
        val existingTask = tasks.get(taskId);
        if (existingTask != null) {
          existingTask.handleAction(action);
        }
        break;
    }
  }

  public Task getTask(String taskId) {
    checkTaskExists(taskId);
    return tasks.get(taskId);
  }

  protected abstract Task createTask(String taskId, String releaseId) throws TaskException;

}
