package io.kf.coordinator.task;

import io.kf.coordinator.task.fsm.config.TaskFSMGenerator;
import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public abstract class Task {

  protected StateMachine<TaskFSMStates, TaskFSMEvents> stateMachine;
  @NonNull
  @Getter
  protected String id;
  @Getter
  protected String release;

  public Task(String id, String release) throws Exception {
    this.id = id;
    this.release = release;

    stateMachine = TaskFSMGenerator.generate();
  }

  public void handleAction(TaskAction action) {
    switch(action) {
      case initialize:
        this.initialize();
        break;
      case cancel:
        this.cancel();
        break;
      case start:
        this.run();
        break;
      case publish:
        this.publish();
        break;
    }
  }

  public abstract void initialize();
  public abstract void run();
  public abstract void publish();
  public abstract void cancel();

  public TaskFSMStates getState() {
    return stateMachine.getState().getId();
  }

  public float getProgress() {
    return 0;
  }

}