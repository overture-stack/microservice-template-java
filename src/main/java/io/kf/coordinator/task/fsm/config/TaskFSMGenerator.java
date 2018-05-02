package io.kf.coordinator.task.fsm.config;

import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

@Slf4j
public class TaskFSMGenerator {

  public static StateMachine<TaskFSMStates, TaskFSMEvents> generate() throws Exception {
    val builder = StateMachineBuilder.<TaskFSMStates, TaskFSMEvents>builder();
    builder.configureStates().withStates()
      .initial(TaskFSMStates.READY)
      .state(TaskFSMStates.PENDING)
      .state(TaskFSMStates.RUNNING)
      .state(TaskFSMStates.STAGED)
      .state(TaskFSMStates.PUBLISHING)
      .state(TaskFSMStates.PUBLISHED)
      .state(TaskFSMStates.CANCELED)
      .state(TaskFSMStates.FAILED);

    builder.configureTransitions()
      .withExternal()
        .source(TaskFSMStates.READY)
        .target(TaskFSMStates.PENDING)
        .event(TaskFSMEvents.INITIALIZE)
      .and()
      .withExternal()
        .source(TaskFSMStates.PENDING)
        .target(TaskFSMStates.RUNNING)
        .event(TaskFSMEvents.RUN)
      .and()
      .withExternal()
        .source(TaskFSMStates.RUNNING)
        .target(TaskFSMStates.STAGED)
        .event(TaskFSMEvents.RUNNING_DONE)
      .and()
      .withExternal()
        .source(TaskFSMStates.STAGED)
        .target(TaskFSMStates.PUBLISHING)
        .event(TaskFSMEvents.PUBLISH)
      .and()
      .withExternal()
        .source(TaskFSMStates.PUBLISHING)
        .target(TaskFSMStates.PUBLISHED)
        .event(TaskFSMEvents.PUBLISHING_DONE)

      // RE-INITIALIZE Transitions
      .and()
      .withExternal()
        .source(TaskFSMStates.CANCELED)
        .target(TaskFSMStates.PENDING)
        .event(TaskFSMEvents.INITIALIZE)
      .and()
      .withExternal()
        .source(TaskFSMStates.FAILED)
        .target(TaskFSMStates.PENDING)
        .event(TaskFSMEvents.INITIALIZE)

      // CANCEL Transitions
      .and()
      .withExternal()
        .source(TaskFSMStates.READY)
        .target(TaskFSMStates.CANCELED)
        .event(TaskFSMEvents.CANCEL)
      .and()
      .withExternal()
        .source(TaskFSMStates.PENDING)
        .target(TaskFSMStates.CANCELED)
        .event(TaskFSMEvents.CANCEL)
      .and()
        .withExternal()
        .source(TaskFSMStates.RUNNING)
        .target(TaskFSMStates.CANCELED)
        .event(TaskFSMEvents.CANCEL)
      .and()
      .withExternal()
        .source(TaskFSMStates.STAGED)
        .target(TaskFSMStates.CANCELED)
        .event(TaskFSMEvents.CANCEL)

      // FAIL Transitions
      .and()
      .withExternal()
        .source(TaskFSMStates.READY)
        .target(TaskFSMStates.FAILED)
        .event(TaskFSMEvents.FAIL)
      .and()
      .withExternal()
        .source(TaskFSMStates.RUNNING)
        .target(TaskFSMStates.FAILED)
        .event(TaskFSMEvents.FAIL)
      .and()
      .withExternal()
        .source(TaskFSMStates.PUBLISHING)
        .target(TaskFSMStates.FAILED)
        .event(TaskFSMEvents.FAIL);

    val fsm = builder.build();
    fsm.start();
    return fsm;
  }
}
