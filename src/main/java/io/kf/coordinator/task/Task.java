package io.kf.coordinator.task;

import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import io.kf.coordinator.task.fsm.config.TaskFSMGenerator;
import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public abstract class Task {

  private StateMachine<TaskFSMStates, TaskFSMEvents> stateMachine;
  @NonNull
  @Getter
  private String id;
  @Getter
  private String release;

  public Task(String id, String release) throws Exception {
    this.id = id;
    this.release = release;

    stateMachine = TaskFSMGenerator.generate(
      ()->this.initialize(),
      ()->this.run(),
      ()->this.publish()
    );

  }

  public void handleAction(TaskAction action) {
    switch(action) {
      case cancel:
        this.stateMachine.sendEvent(TaskFSMEvents.CANCEL);
        break;
      case start:
        this.stateMachine.sendEvent(TaskFSMEvents.START);
        break;
      case publish:
        this.stateMachine.sendEvent(TaskFSMEvents.PUBLISH);
    }
  }

  public abstract Void initialize();
  public abstract Void run();
  public abstract Void publish();

  public TaskFSMStates getState() {
    return this.stateMachine.getState().getId();
  }

  public float getProgress() {
    return 0;
  }
}