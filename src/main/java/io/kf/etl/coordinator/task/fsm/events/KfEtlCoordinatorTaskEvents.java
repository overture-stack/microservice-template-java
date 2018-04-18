package io.kf.etl.coordinator.task.fsm.events;

public enum KfEtlCoordinatorTaskEvents {
    INITIALIZE, START, PUBLISH, CANCEL,
    //the following events are defined only for internal use
    RUNNING_DONE, PUBLISHING_DONE
}
