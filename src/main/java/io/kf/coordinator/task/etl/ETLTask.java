package io.kf.coordinator.task.etl;

import com.spotify.docker.client.exceptions.DockerException;
import io.kf.coordinator.exceptions.IllegalEventRequestException;
import io.kf.coordinator.task.Task;
import io.kf.coordinator.task.fsm.events.TaskFSMEvents;
import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.CANCEL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.FAIL;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.INITIALIZE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISH;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.PUBLISHING_DONE;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUN;
import static io.kf.coordinator.task.fsm.events.TaskFSMEvents.RUNNING_DONE;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.CANCELED;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.FAILED;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.PUBLISHED;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.PUBLISHING;
import static io.kf.coordinator.task.fsm.states.TaskFSMStates.RUNNING;
import static io.kf.coordinator.utils.Strings.extractStackTrace;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
public class ETLTask extends Task{

  private final ETLDockerContainer etl;
  private final Set<String> studyIds;

  @Getter
  @JsonIgnore
  private final String displayName;

  public ETLTask(@NonNull ETLDockerContainer etl,
          @NonNull String id,
          @NonNull String releaseId,
          @NonNull Set<String> studyIds) throws Exception {
    super(id, releaseId);
    this.etl = etl;
    this.studyIds = studyIds;
    this.displayName = format("ETL Task [%s]", id);
  }

  //TODO: sanitize studyIds and release at instantiation phase and if not good, put in REJECT state

  @Override
  public void initialize() {
    log.info("{} Initializing ...", getDisplayName());
    if (!studyIds.isEmpty() && isNotBlank(release) ){
      try {
        etl.createContainer(studyIds, release);
        sendEvent(INITIALIZE);
        log.info("{} -> PENDING.", getDisplayName());
      } catch (Throwable t) {
        fail();
        log.info("{} -> FAILED while initializing. [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    } else {
      fail();
      //TODO: this should probably be the reject state and not the fail state
      log.info("{} -> FAILED while initializing because studies empty or release is blank.", getDisplayName());
    }

  }

  @Override
  public void run() {
    log.info("{} Running ...", getDisplayName());
    sendEvent(RUN);

    try {
      etl.runETL();

    } catch (Throwable t) {
      fail();
      log.info("{} -> FAILED while running. [{}]: {} -> {}",
          getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
    }
    log.info("{} started.", getDisplayName());
  }

  @Override
  public TaskFSMStates getState() {

    if(stateMachine.getState().getId().equals(RUNNING)){
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

  public void fail() {
    sendEvent(FAIL);
    try{
      etl.killAndRemove();
    } catch (Throwable t){
      log.error("Failed to kill and remove [{}]: {} -> {}",
          t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t) );
    }
  }

  @Override
  public void publish() {
    log.info("{} Publishing ...", getDisplayName());
    sendEvent2(PUBLISH, PUBLISHING, () -> {
          boolean rollCallResult = executeRollCall();
          if (rollCallResult) {
            sendEvent2(PUBLISHING_DONE, PUBLISHED);
            log.info("{} -> PUBLISHED.", getDisplayName());
          } else {
            fail();
            log.info("{} -> PUBLISHING failed", getDisplayName());
          }
        }
    );
  }

  private boolean executeRollCall(){
    // Stub. Make call to RollCall, and return true if a 200, and false otherwise
    throw new IllegalEventRequestException("RollCall not incorporated yet");
  }

  @Override
  public void cancel() {
    log.info("{} Cancelling ...", getDisplayName());
    // uncomment below when implement REJECT state
//    if(!stateMachine.getState().getId().equals(READY)){

    // execute cancel only if not in cancelled state
    sendEvent2(CANCEL, CANCELED, () -> {
      try {
        etl.killAndRemove();
        log.info("{} has been cancelled.", getDisplayName());
      } catch (Throwable t){
        sendEvent(FAIL, FAILED);
        log.info("{} -> FAILED to cancel. [{}]: {} -> {}",
            getDisplayName(), t.getClass().getSimpleName(), t.getMessage(), extractStackTrace(t));
      }
    });
//    }
  }

  private void sendEvent2(TaskFSMEvents event, TaskFSMStates targetState){
    sendEvent2(event, targetState, () ->  null);
  }

  private void sendEvent2(TaskFSMEvents event, TaskFSMStates targetState, Runnable runnable){
    sendEvent2(event, targetState, () -> {
      runnable.run();
      return Optional.empty();
    });
  }

  private boolean isTransitionable(TaskFSMStates sourceState, TaskFSMEvents event){
    return stateMachine.getTransitions().stream()
        .filter(x -> x.getSource().getId().equals(sourceState))
        .anyMatch(x -> x.getTrigger().getEvent().equals(event));
  }

  private boolean isIdempotentEvent(TaskFSMStates sourceState, TaskFSMEvents event){
    val idempotent = stateMachine.getTransitions().stream()
        .filter(x -> x.getTrigger().getEvent().equals(event))
        .anyMatch(x -> x.getTarget().getId().equals(sourceState));
//    checkState(idempotent || isTransitionable(sourceState, event),
//        "Cannot transition from source state '%s' with event '%s'", sourceState, event);
    return idempotent;
  }

  private void sendEvent4(TaskFSMEvents event, Runnable runnable){
    sendEvent4(event, () -> {
      runnable.run();
      return true;
    });
  }
  private <R> Optional<R> sendEvent4(TaskFSMEvents event, Supplier<R> callback){
    val currState = stateMachine.getState().getId();
    val idempotent = isIdempotentEvent(currState, event);
    if (idempotent){
      return Optional.empty();
    } else if (!stateMachine.sendEvent(event)){
      val message = format("ERROR %s: the current state '%s' cannot process the event '%s'",
          getDisplayName(), stateMachine.getState().getId(), event);
      throw new IllegalEventRequestException(message);
    }
    return Optional.of(callback.get());
  }

  private <R> Optional<R> sendEvent2(TaskFSMEvents event, TaskFSMStates targetState, Supplier<R> callback){
    if (stateMachine.getState().getId().equals(targetState) ){
      log.info("{} did not send event '{}' since already in '{}' state",
          getDisplayName(), event, targetState);
      return Optional.empty();
    } else if (!stateMachine.sendEvent(event)){
      val message = format("ERROR %s: the current state '%s' cannot process the event '%s'",
          getDisplayName(), stateMachine.getState().getId(), event);
      throw new IllegalEventRequestException(message);
    }
    return Optional.of(callback.get());
  }
  private boolean sendEvent(TaskFSMEvents event, TaskFSMStates targetState){
    if (stateMachine.getState().getId().equals(targetState) ){
      log.info("{} did not send event '{}' since already in '{}' state",
          getDisplayName(), event, targetState);
      return false;
    } else if (!stateMachine.sendEvent(event)){
      val message = format("ERROR %s: the current state '%s' cannot process the event '%s'",
          getDisplayName(), stateMachine.getState().getId(), event);
      throw new IllegalEventRequestException(message);
    }
    return true;
  }
  private boolean sendEvent(TaskFSMEvents event){
    if (!stateMachine.sendEvent(event)){
      val message = format("ERROR %s: the current state '%s' cannot process the event '%s'",
          getDisplayName(), stateMachine.getState().getId(), event);
      throw new IllegalEventRequestException(message);
    }
    return true;
  }

}
