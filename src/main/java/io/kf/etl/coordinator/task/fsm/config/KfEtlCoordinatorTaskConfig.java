package io.kf.etl.coordinator.task.fsm.config;

import io.kf.etl.coordinator.task.fsm.events.KfEtlCoordinatorTaskEvents;
import io.kf.etl.coordinator.task.fsm.states.KfEtlCoordinatorTaskStates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.statemachine.StateMachine;

@Configuration
@Slf4j
public class KfEtlCoordinatorTaskConfig {

    @Bean
    @Scope("singleton")
    public StateMachine<KfEtlCoordinatorTaskStates, KfEtlCoordinatorTaskEvents> getFSM() throws Exception {
        return FSMGenerator.generate();
    }

}
