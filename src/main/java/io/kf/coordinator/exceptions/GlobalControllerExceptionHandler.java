package io.kf.coordinator.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

import static io.kf.coordinator.utils.Collectors.toImmutableList;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

  private static final String AMPERSAND = "&";
  private static final String QUESTION_MARK = "?";


  @ExceptionHandler(IllegalEventRequestException.class)
  public ResponseEntity<TaskTransitionError> handleTransitionError
    (HttpServletRequest request, HttpServletResponse response, IllegalEventRequestException ex){
    return ResponseEntity
        .status(BAD_REQUEST)
        .body(TaskTransitionError.builder()
            .message(ex.getMessage())
            .status(BAD_REQUEST)
            .requestUrl(generateRequestUrlWithParams(request))
            .timestamp(System.currentTimeMillis())
            .build());
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TaskTransitionError{
    private String requestUrl;
    private String message;
    private HttpStatus status;
    private long timestamp;
  }


  private static String generateRequestUrlWithParams(HttpServletRequest request){
    val requestUrl = request.getRequestURL().toString();
    val paramEntries = request.getParameterMap().entrySet();
    if (paramEntries.size() > 0){
      val params = paramEntries.stream()
          .map(x -> createUrlParams(x.getKey(), x.getValue()))
          .flatMap(Collection::stream)
          .collect(joining(AMPERSAND));
      return requestUrl+QUESTION_MARK+params;
    }
    return requestUrl;
  }

  private static List<String> createUrlParams(String key, String ... values){
    return stream(values)
        .map(x -> createUrlParam(key, x))
        .collect(toImmutableList());
  }

  private static String createUrlParam(String key, String value){
    return format("%s=%s", key, value);
  }

}
