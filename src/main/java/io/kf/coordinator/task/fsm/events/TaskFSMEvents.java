package io.kf.coordinator.task.fsm.events;

public enum TaskFSMEvents {
    INITIALIZE, RUN, PUBLISH, CANCEL, FAIL,
    //the following events are defined only for internal use
    RUNNING_DONE, PUBLISHING_DONE
}
