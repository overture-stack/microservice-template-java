package io.kf.coordinator.task;

import com.google.common.collect.ImmutableList;
import io.kf.coordinator.exceptions.TaskException;
import io.kf.coordinator.exceptions.TaskManagerException;
import io.kf.coordinator.exceptions.TaskNotFoundException;
import io.kf.coordinator.model.TasksRequest;
import io.kf.coordinator.model.dto.TasksDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static com.google.common.collect.Queues.newLinkedBlockingQueue;
import static io.kf.coordinator.exceptions.TaskManagerException.checkTaskManager;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public abstract class TaskManager {
  private static final String ETL_MANAGER = "ETL Manager:";

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

  public void dispatch(TasksRequest request) {
    val taskId = request.getTask_id();
    val releaseId = request.getRelease_id();
    val action = request.getAction();

    switch(action) {

      case status:
        // No action to take
        break;

      case initialize:
        log.debug("{} Initialize action for {}", ETL_MANAGER, request.getTask_id());
        try{
          val task = registerTask(taskId, releaseId);
          task.handleAction(action);
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
          existingTask.handleAction(action);
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
  private Task registerTask(String taskId, String releaseId){
    val existingTaskResult = findTask(taskId);
    val newTask = createTask(taskId, releaseId);
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
    //            checkTaskManager(!isQueueFull(),
    //                  "The task queue is full with a max size of %s", maxQueueSize);
    //            runningTaskQueue.add(newTask.getId());
    addTask(task);
    return task;
  }

  protected abstract Task createTask(String taskId, String releaseId) throws TaskException;

}
