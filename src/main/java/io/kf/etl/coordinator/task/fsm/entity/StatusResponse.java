package io.kf.etl.coordinator.task.fsm.entity;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusResponse {
    @NonNull
    private String name;
    @NonNull
    private String message;
    @NonNull
    private String version;
}
