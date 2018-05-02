package io.kf.coordinator.task.etl;

import com.spotify.docker.client.exceptions.DockerException;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ETLTask extends Task{

  private ETLDockerContainer etl;

  public ETLTask(String id, String release) throws Exception {
    super(id, release);

    etl = new ETLDockerContainer();
  }

  @Override
  public void initialize() {
    log.info(String.format("ETL Task [%s] Initializing ...", this.id));

    try {

      etl.startContainer();

      this.stateMachine.sendEvent(TaskFSMEvents.INITIALIZE);
      log.info(String.format("ETL Task [%s] -> PENDING.", this.id));

    } catch (InterruptedException e) {
      e.printStackTrace();
      this.stateMachine.sendEvent(TaskFSMEvents.FAIL);
      log.info(String.format("ETL Task [%s] -> FAILED while initializing.", this.id));

    } catch (DockerException e) {
      e.printStackTrace();
      this.stateMachine.sendEvent(TaskFSMEvents.FAIL);
      log.info(String.format("ETL Task [%s] -> FAILED while initializing.", this.id));
    }
  }

  @Override
  public void run() {
    log.info(String.format("ETL Task [%s] Running ...", this.id));
    boolean startedRunning = this.stateMachine.sendEvent(TaskFSMEvents.RUN);

    if(startedRunning) {
      try {
        etl.runETL();
        this.stateMachine.sendEvent(TaskFSMEvents.RUNNING_DONE);
        log.info(String.format("ETL Task [%s] -> STAGED.", this.id));

      } catch (InterruptedException e) {
        e.printStackTrace();
        this.stateMachine.sendEvent(TaskFSMEvents.FAIL);
        log.info(String.format("ETL Task [%s] -> FAILED while running.", this.id));

      } catch (DockerException e) {
        e.printStackTrace();
        this.stateMachine.sendEvent(TaskFSMEvents.FAIL);
        log.info(String.format("ETL Task [%s] -> FAILED while running.", this.id));
      }
      this.stateMachine.sendEvent(TaskFSMEvents.RUN);

      log.info(String.format("ETL Task [%s] started.", this.id));

    }

  }

  @Override
  public TaskFSMStates getState() {

    if(super.getState().equals(TaskFSMStates.RUNNING)){
      // Check if docker has stopped
      boolean isComplete = this.etl.isComplete();



      if (hasStopped) {
        this.stateMachine.sendEvent(TaskFSMEvents.RUNNING_DONE);
      }
    }

    return super.getState();
  }

  @Override
  public void publish() {
    if(this.stateMachine.sendEvent(TaskFSMEvents.PUBLISH)) {
      log.info(String.format("ETL Task [%s] Publishing ...", this.id));

      if (this.stateMachine.sendEvent(TaskFSMEvents.PUBLISHING_DONE)) {
        log.info(String.format("ETL Task [%s] -> PUBLISHED.", this.id));
      }
    }
  }

  @Override
  public void cancel() {
    // If running, stop docker,
    if(this.stateMachine.sendEvent(TaskFSMEvents.CANCEL)) {
      log.info(String.format("ETL Task [%s] has been cancelled.", this.id));
    }
  }
}
