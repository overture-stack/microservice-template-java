package io.kf.coordinator.task.fsm.events;

public enum TaskFSMEvents {
    INITIALIZE, START, PUBLISH, CANCEL,
    //the following events are defined only for internal use
    RUNNING_DONE, PUBLISHING_DONE
}
