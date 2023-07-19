package io.mapsmessaging.devices.sensorreadings;

public class ComputationResult<T> {

  private final T result;
  private final Exception error;

  private ComputationResult(T result, Exception error) {
    this.result = result;
    this.error = error;
  }

  public static <T> ComputationResult<T> success(T result) {
    return new ComputationResult<>(result, null);
  }

  public static <T> ComputationResult<T> failure(Exception error) {
    return new ComputationResult<>(null, error);
  }

  public boolean hasError() {
    return error != null;
  }

  public T getResult() {
    return result;
  }

  public Exception getError() {
    return error;
  }
}
