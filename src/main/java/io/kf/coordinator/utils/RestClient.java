package io.kf.coordinator.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;


@RequiredArgsConstructor
public class RestClient {

  @NonNull private final RestTemplate template;
  private final boolean useAuth;

  public static <T> Optional<ResponseEntity<T>> tryNotFoundRequest(Supplier<ResponseEntity<T>> restCallback){
    ResponseEntity<T> response;
    try{
      response = restCallback.get();
    } catch (HttpClientErrorException e){
      if (e.getRawStatusCode() == NOT_FOUND.getStatusCode()){
        return Optional.empty();
      }
      throw e;
    }
    return Optional.of(response);
  }

  public <R> ResponseEntity<R> get(String accessToken, @NonNull String url, @NonNull Class<R> responseType){
    return template.exchange(url, GET, emptyEntity(accessToken), responseType);
  }

  public <R> ResponseEntity<List<R>> gets(String accessToken, @NonNull String url, @NonNull Class<R> responseElementType){
    return template.exchange(url, GET, emptyEntity(accessToken),
        createListParameterizedTypeReference(responseElementType));
  }

  public <R, T> ResponseEntity<R> post(String accessToken, @NonNull String url,
      @NonNull T body, @NonNull Class<R> responseType){
    return template.exchange(url, POST, bodyEntity(accessToken, body), responseType);
  }

  public <R, T> ResponseEntity<List<R>> posts(String accessToken, @NonNull String url,
    @NonNull T body, @NonNull Class<R> responseElementType){
    return template.exchange(url, POST, bodyEntity(accessToken, body),
        createListParameterizedTypeReference(responseElementType));
  }

  private <T> HttpEntity<T> bodyEntity(String accessToken, @NonNull T body){
    return buildEntity(accessToken, body);
  }

  private HttpEntity<String> emptyEntity(String accessToken){
    return buildEntity(accessToken, null);
  }

  private <T> HttpEntity<T> buildEntity(String accessToken, T body){
    return new HttpEntity<>(body, buildHttpHeader(accessToken));
  }

  private HttpHeaders buildHttpHeader(String accessToken){
    val headers = new HttpHeaders();
    if (useAuth){
      if (isNullOrEmpty(accessToken)){
        throw new HttpClientErrorException(UNAUTHORIZED, "The authorization token cannot be null or empty");
      }
      headers.set(AUTHORIZATION, accessToken);
    }
    headers.setContentType(APPLICATION_JSON_UTF8);
    return headers;
  }

  private static <T> ParameterizedTypeReference<T> createListParameterizedTypeReference(@NonNull Class<?> elementType){
    return new ParameterizedTypeReference<T>() {

      @Override
      public Type getType() {
        return ParameterizedTypeImpl.make(
            List.class, //Wrapper object
            new Type[]{elementType}, //Class of object that is wrapped by the Wrapper
            null
        );
      }
    };
  }


}
