package io.kf.coordinator.dto;

import io.kf.coordinator.task.fsm.states.TaskFSMStates;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Builder
public class TasksDTO {

  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
  public static String formatDate(Date date ) {
    return dateFormatter.format(date);
  }

  @Builder.Default private String name = "KF ETL Task Runner!";
                   private String task_id;
                   private String release_id;
                   private TaskFSMStates state;
                   private float progress;
  @Builder.Default private String date_submitted = formatDate(new Date());

  public String getState() {
    // Lowercase states to match spec
    return this.state.name().toLowerCase();
  }
}
