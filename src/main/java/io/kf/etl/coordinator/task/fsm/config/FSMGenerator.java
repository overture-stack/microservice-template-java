package io.kf.etl.coordinator.task.fsm.config;

import io.kf.etl.coordinator.task.fsm.events.KfEtlCoordinatorTaskEvents;
import io.kf.etl.coordinator.task.fsm.states.KfEtlCoordinatorTaskStates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import static io.kf.etl.coordinator.task.fsm.events.KfEtlCoordinatorTaskEvents.*;
import static io.kf.etl.coordinator.task.fsm.states.KfEtlCoordinatorTaskStates.*;

@Slf4j
public class FSMGenerator {

  public static StateMachine<KfEtlCoordinatorTaskStates, KfEtlCoordinatorTaskEvents> generate() throws Exception {
    val builder = StateMachineBuilder.<KfEtlCoordinatorTaskStates, KfEtlCoordinatorTaskEvents>builder();
    builder.configureStates().withStates()
      .initial(READY).stateDo(READY, context-> {
        log.info("Initializing State Machine ... setting to pending");
        context.getStateMachine().sendEvent(INITIALIZE);
        log.info("Pending further action.");
    })
      .state(PENDING)
      .state(RUNNING).stateDo(RUNNING, context -> {
        log.info("Running ETL ... ");
        context.getStateMachine().sendEvent(RUNNING_DONE);
        log.info("ETL is done. ");
      })
      .state(STAGED)
      .state(PUBLISHING).stateDo(PUBLISHING, context -> {
        context.getStateMachine().sendEvent(PUBLISHING_DONE);
      })
      .state(PUBLISHED);

    builder.configureTransitions()
      .withExternal()
        .source(READY)
        .target(PENDING)
        .event(INITIALIZE)
      .and()
      .withExternal()
        .source(PENDING)
        .target(RUNNING)
        .event(START)
      .and()
      .withExternal()
        .source(RUNNING)
        .target(STAGED)
        .event(RUNNING_DONE)
      .and()
      .withExternal()
        .source(STAGED)
        .target(PUBLISHING)
        .event(PUBLISH)
      .and()
      .withExternal()
        .source(PUBLISHING)
        .target(PUBLISHED)
        .event(PUBLISHING_DONE)
      .and()
      .withExternal()
        .source(PUBLISHED)
        .target(PENDING)
        .event(INITIALIZE)
      .and()
      .withExternal()
        .source(CANCELED)
        .target(PENDING)
        .event(INITIALIZE)
      .and()
      .withExternal()
        .source(FAILED)
        .target(PENDING)
        .event(INITIALIZE)
      .and()
      .withExternal()
        .source(PENDING)
        .target(CANCELED)
        .event(CANCEL);

    val fsm = builder.build();
    fsm.start();
    return fsm;
  }
}
