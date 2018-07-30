package io.kf.coordinator.controller;

import io.kf.coordinator.dto.TasksRequest;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.dto.StatusDTO;
import io.kf.coordinator.dto.TasksDTO;
import io.kf.coordinator.task.etl.ETLTaskManager;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

@RestController
public class ETLCoordinatorTaskController {

  private String STATUS_READY = "ready for work";
  private String STATUS_UNAVAILABLE = "service unavailable";

  @Autowired
  private ETLTaskManager taskManager;

  @GetMapping(path = "/status")
  @Produces("application/json")
  public @ResponseBody
  StatusDTO status() {

    val status = StatusDTO.builder()
      //TODO: Logic to check if service is available
      .message(STATUS_READY);

    return status.build();
  }

  @PostMapping(path = "/tasks")
  @Consumes("application/json")
  @Produces("application/json")
  public @ResponseBody
  TasksDTO tasks(
    @RequestBody(required = true) TasksRequest request
  ) {

    taskManager.dispatch(request);

    Task task = taskManager.getTask(request.getTask_id());

    // TODO: Handle task == null case

    val response = TasksDTO.builder()
      .task_id(request.getTask_id())
      .release_id(request.getRelease_id())
      .state(task.getState())
      .progress(task.getProgress());
    return response.build();
  }

}
