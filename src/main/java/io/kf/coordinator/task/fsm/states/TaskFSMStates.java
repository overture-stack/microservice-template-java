package io.kf.coordinator.task.fsm.states;

public enum TaskFSMStates {
    READY, PENDING, RUNNING, STAGED, PUBLISHING, PUBLISHED, CANCELED, FAILED
}
