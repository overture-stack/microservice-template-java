package io.kf.etl.coordinator.task.fsm.entity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TasksResponse {
    @NonNull
    private String name;
    @NonNull
    private String kf_id;
    @NonNull
    private String release_id;
    @NonNull
    private String state;
    @NonNull
    private String progress;
    @NonNull
    private String date_submitted;
}
