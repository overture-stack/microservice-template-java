package io.kf.etl.coordinator.task.controller;

import io.kf.etl.coordinator.task.fsm.entity.StatusResponse;
import io.kf.etl.coordinator.task.fsm.entity.TasksRequest;
import io.kf.etl.coordinator.task.fsm.entity.TasksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

@RestController
public class KfEtlCoordinatorTaskController {

    @GetMapping(path = "/status/")
    @Produces("application/json")
    public ResponseEntity<StatusResponse> status(){
        return null;
    }

    @PostMapping(path = "/tasks/")
    @Consumes("application/json")
    @Produces("application/json")
    public ResponseEntity<TasksResponse> tasks(@RequestBody TasksRequest request) {
        return null;
    }
}
