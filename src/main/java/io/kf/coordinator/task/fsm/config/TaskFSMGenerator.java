package io.kf.coordinator.task.fsm.config;

import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import java.util.concurrent.Callable;

@Slf4j
public class TaskFSMGenerator {

  public static StateMachine<TaskFSMStates, TaskFSMEvents> generate(Callable<Void> initialize, Callable<Void> run, Callable<Void> publish) throws Exception {
    val builder = StateMachineBuilder.<TaskFSMStates, TaskFSMEvents>builder();
    builder.configureStates().withStates()
      .initial(TaskFSMStates.READY).stateDo(TaskFSMStates.READY, context-> {
        log.info("Initializing State Machine ... setting to pending");
        context.getStateMachine().sendEvent(TaskFSMEvents.INITIALIZE);
        log.info("Pending further action.");
    })
      .state(TaskFSMStates.PENDING)
      .state(TaskFSMStates.RUNNING).stateDo(TaskFSMStates.RUNNING, context -> {
        log.info("Running ETL ... ");
        context.getStateMachine().sendEvent(TaskFSMEvents.RUNNING_DONE);
        log.info("ETL is done. ");
      })
      .state(TaskFSMStates.STAGED)
      .state(TaskFSMStates.PUBLISHING).stateDo(TaskFSMStates.PUBLISHING, context -> {
        context.getStateMachine().sendEvent(TaskFSMEvents.PUBLISHING_DONE);
      })
      .state(TaskFSMStates.PUBLISHED);

    builder.configureTransitions()
      .withExternal()
        .source(TaskFSMStates.READY)
        .target(TaskFSMStates.PENDING)
        .event(TaskFSMEvents.INITIALIZE)
      .and()
      .withExternal()
        .source(TaskFSMStates.PENDING)
        .target(TaskFSMStates.RUNNING)
        .event(TaskFSMEvents.START)
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
      .and()
      .withExternal()
        .source(TaskFSMStates.PUBLISHED)
        .target(TaskFSMStates.PENDING)
        .event(TaskFSMEvents.INITIALIZE)
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
      .and()
      .withExternal()
        .source(TaskFSMStates.PENDING)
        .target(TaskFSMStates.CANCELED)
        .event(TaskFSMEvents.CANCEL);

    val fsm = builder.build();
    fsm.start();
    return fsm;
  }
}
