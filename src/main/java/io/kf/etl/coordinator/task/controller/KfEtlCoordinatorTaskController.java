package io.kf.etl.coordinator.task.controller;

import io.kf.etl.coordinator.task.Task;
import io.kf.etl.coordinator.task.TaskManager;
import io.kf.etl.coordinator.task.dto.StatusDTO;
import io.kf.etl.coordinator.task.dto.TasksDTO;
import io.kf.etl.coordinator.task.dto.TasksRequest;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

@RestController
public class KfEtlCoordinatorTaskController {

  private String STATUS_READY = "ready for work";
  private String STATUS_UNAVAILABLE = "service unavailable";

  @Autowired
  private TaskManager taskManager;

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
    @RequestBody(required = true) Task  sRequest request
  ) {

    taskManager.dispatch(request);

    Task task = taskManager.getTask(request.getTask_id());

    val response = TasksDTO.builder()
      .task_id(request.getTask_id())
      .release_id(request.getRelease_id())
      .state(task.getState())
      .progress(task.getProgress());
    return response.build();

  }
}
