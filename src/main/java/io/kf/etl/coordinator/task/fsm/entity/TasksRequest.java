package io.kf.etl.coordinator.task.fsm.entity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TasksRequest {

    @NonNull
    private String action;
    @NonNull
    private String task_id;
    @NonNull
    private String release_id;
}
