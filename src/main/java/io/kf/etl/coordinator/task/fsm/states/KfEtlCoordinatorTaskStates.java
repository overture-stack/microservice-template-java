package io.kf.etl.coordinator.task.fsm.states;

public enum KfEtlCoordinatorTaskStates {
    READY, PENDING, RUNNING, STAGED, PUBLISHING, PUBLISHED, CANCELED, FAILED
}
