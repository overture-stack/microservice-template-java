package io.kf.etl.coordinator.task;

import io.kf.etl.coordinator.task.fsm.config.FSMGenerator;
import io.kf.etl.coordinator.task.fsm.events.KfEtlCoordinatorTaskEvents;
import io.kf.etl.coordinator.task.fsm.states.KfEtlCoordinatorTaskStates;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;

@Slf4j
public class Task {

  private StateMachine<KfEtlCoordinatorTaskStates, KfEtlCoordinatorTaskEvents> stateMachine;
  @NonNull
  @Getter
  private String id;
  @Getter
  private String release;

  public Task(String id, String release) throws Exception {
    stateMachine = FSMGenerator.generate();
    this.id = id;
    this.release = release;
  }

  public void handleAction(TaskAction action) {
    switch(action) {
      case cancel:
        this.stateMachine.sendEvent(KfEtlCoordinatorTaskEvents.CANCEL);
        break;
      case start:
        this.stateMachine.sendEvent(KfEtlCoordinatorTaskEvents.START);
        break;
      case publish:
        this.stateMachine.sendEvent(KfEtlCoordinatorTaskEvents.PUBLISH);
    }
  }



  public KfEtlCoordinatorTaskStates getState() {
    return this.stateMachine.getState().getId();
  }

  public float getProgress() {
    return 0;
  }
}