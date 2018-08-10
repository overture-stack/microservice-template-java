package io.kf.coordinator.task.etl;

import com.spotify.docker.client.exceptions.DockerException;
import io.kf.coordinator.exceptions.IllegalEventRequestException;
import io.kf.coordinator.service.PublishService;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Set;

import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.CANCEL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.FAIL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.INITIALIZE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISH;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISHING_DONE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUN;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUNNING_DONE;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.RUNNING;
import static io.kf.coordinator.utils.Strings.extractStackTrace;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
public class ETLTask extends Task {

  /**
   * Config
   */
  @Getter private final Set<String> studies;
  @Getter @JsonIgnore private final String displayName;

  /**
   * Dependencies
   */
  private final ETLDockerContainer etl;
  private final PublishService publishService;

  public ETLTask(@NonNull ETLDockerContainer etl,
      @NonNull PublishService publishService,
      @NonNull String id,
      @NonNull String release,
      @NonNull Set<String> studies) throws Exception {
    super(id, release);
    this.etl = etl;
    this.studies = studies;
    this.displayName = format("ETL Task [%s]", id);
    this.publishService = publishService;
  }

  //TODO: sanitize studyIds and release at instantiation phase and if not good, put in REJECT state
  @Override
  public void initialize() {
    log.info("{} Initializing ...", getDisplayName());
    if (!getStudies().isEmpty() && isNotBlank(getRelease())) {
      sendEvent(INITIALIZE, new EventCallback() {

        @Override public void onRun() throws Throwable {
          etl.createContainer(getStudies(), getRelease());
          log.info("{} -> PENDING.", getDisplayName());
        }

        @Override public void onError(Throwable t) {
          fail();
          log.info("{} -> FAILED while initializing. [{}]: {} -> {}",
              getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
        }
      });
    } else {
      fail();
      //TODO: this should probably be the reject state and not the fail state
      log.info("{} -> FAILED while initializing because studies empty or release is blank.", getDisplayName());
    }

  }

  @Override
  public void run() {
    log.info("{} Running ...", getDisplayName());
    sendEvent(RUN, new EventCallback() {

      @Override public void onRun() throws Throwable {
        etl.runETL();
        log.info("{} started.", getDisplayName());
      }

      @Override public void onError(Throwable t) {
        fail();
        log.info("{} -> FAILED while running. [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    });
  }

  @Override
  public TaskFSMStates getState() {

    if (stateMachine.getState().getId().equals(RUNNING)) {
      // Check if docker has stopped
      try {
        val isComplete = etl.isComplete();
        if (isComplete) {
          if (etl.finishedWithErrors()) {
            fail();
            log.info("{} -> ETL finished with errors", getDisplayName());
          } else {
            sendEvent(RUNNING_DONE);
            log.info("{} -> ETL finished successfully without errors", getDisplayName());
          }
        }
      } catch (InterruptedException | DockerException e) {
        fail();
      }
    }

    return stateMachine.getState().getId();
  }

  @Override
  public void fail() {
    sendEvent(FAIL, new EventCallback() {

      @Override public void onRun() throws Throwable {
        etl.killAndRemove();
        log.info("{} failed. Killing and removing it", getDisplayName());
      }

      @Override public void onError(Throwable t) {
        sendEvent(FAIL);
        log.error("{} failed to kill and remove [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    });
  }


  @Override
  public void publish(@NonNull String accessToken) {
    log.info("{} Publishing ...", getDisplayName());
    sendEvent(PUBLISH, new EventCallback() {

      @Override public void onRun() throws Throwable {
        publishService.publishRelease(accessToken , getRelease(), getStudies());
        sendEvent(PUBLISHING_DONE);
        log.info("{} -> PUBLISHED.", getDisplayName());
        log.info("{} Cleaning up....", getDisplayName());
        etl.killAndRemove();
        log.info("{} cleaned", getDisplayName());
      }

      @Override public void onError(Throwable t) {
        fail();
        log.info("{} -> PUBLISHING failed due to errors [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    });

  }


  @Override
  public void cancel() {
    log.info("{} Cancelling ...", getDisplayName());
    // uncomment below when implement REJECT state
    //    if(!stateMachine.getState().getId().equals(READY)){

    // execute cancel only if not in cancelled state
    sendEvent(CANCEL, new EventCallback() {

      @Override public void onRun() throws Throwable {
        etl.killAndRemove();
        log.info("{} has been cancelled.", getDisplayName());
      }

      @Override public void onError(Throwable t) {
        fail();
        log.info("{} -> FAILED to cancel. [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    });
    //    }
  }

  boolean isIdempotentEvent(TaskFSMStates sourceState, TaskFSMEvents event) {
    val idempotent = stateMachine.getTransitions().stream()
        .filter(x -> x.getTrigger().getEvent().equals(event))
        .anyMatch(x -> x.getTarget().getId().equals(sourceState));
    return idempotent;
  }

  private void sendEvent(TaskFSMEvents event) {
    if (!stateMachine.sendEvent(event)) {
      val message = format("ERROR %s: the current state '%s' cannot process the event '%s'",
          getDisplayName(), stateMachine.getState().getId(), event);
      log.error(message);
      throw new IllegalEventRequestException(message);
    }
  }

  private void sendEvent(TaskFSMEvents event, EventCallback eventCallback) {
    val currState = stateMachine.getState().getId();
    if (!isIdempotentEvent(currState, event)) {
      sendEvent(event);
      try {
        eventCallback.onRun();
      } catch (Throwable t) {
        eventCallback.onError(t);
      }
    } else {
      log.info("Idempotent event '{}' called for {} in state '{}'. Skipping", event, getDisplayName(), currState );

    }

  }

}
