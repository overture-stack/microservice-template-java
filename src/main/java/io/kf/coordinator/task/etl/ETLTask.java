package io.kf.coordinator.task.etl;

import com.spotify.docker.client.exceptions.DockerException;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.CANCEL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.FAIL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.INITIALIZE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISH;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISHING_DONE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUN;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUNNING_DONE;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.RUNNING;
import static java.lang.String.format;

@Slf4j
public class ETLTask extends Task{

  private final ETLDockerContainer etl;
  private final String studyId;

  public ETLTask(ETLDockerContainer etl, String id, String releaseId, String studyId) throws Exception {
    super(id, releaseId);
    this.etl = etl;
    this.studyId = studyId;
  }

  @Override
  public void initialize() {
    log.info(format("ETL Task [%s] Initializing ...", this.id));

    try {

      etl.startContainer(studyId, release);

      stateMachine.sendEvent(INITIALIZE);
      log.info(format("ETL Task [%s] -> PENDING.", this.id));

    } catch (InterruptedException | DockerException e) {

      e.printStackTrace();
      stateMachine.sendEvent(FAIL);
      log.info(format("ETL Task [%s] -> FAILED while initializing.", this.id));
    }
  }

  @Override
  public void run() {
    log.info(format("ETL Task [%s] Running ...", id));
    boolean startedRunning = stateMachine.sendEvent(RUN);

    if(startedRunning) {
      try {
        etl.runETL();

      } catch (InterruptedException | DockerException e) {
        e.printStackTrace();
        stateMachine.sendEvent(FAIL);
        log.info("ETL Task [{}] -> FAILED while running.", id);
      }
      stateMachine.sendEvent(RUN);

      log.info("ETL Task [{}] started.", id);

    }

  }


  @Override
  public TaskFSMStates getState() {

    if(stateMachine.getState().getId().equals(RUNNING)){
      // Check if docker has stopped
      try {
        val isComplete = etl.isComplete();
        if (isComplete) {
          if (etl.finishedWithErrors()) {
            stateMachine.sendEvent(FAIL);
          } else {
            stateMachine.sendEvent(RUNNING_DONE);
          }
        }
      } catch (InterruptedException | DockerException e) {
        stateMachine.sendEvent(FAIL);
      }
    }

    return stateMachine.getState().getId();
  }

  @Override
  public void publish() {
    if(stateMachine.sendEvent(PUBLISH)) {
      log.info("ETL Task [{}] Publishing ...", id);

      if (stateMachine.sendEvent(PUBLISHING_DONE)) {
        log.info("ETL Task [{}] -> PUBLISHED.", id);
      }
    }
  }

  @Override
  public void cancel() {
    // If running, stop docker,
    if(stateMachine.sendEvent(CANCEL)) {
      log.info(format("ETL Task [%s] has been cancelled.", this.id));
      //rtisma   etl.cancel();
    }
  }
}
