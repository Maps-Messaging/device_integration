package io.mapsmessaging.devices.sensorreadings;

import java.io.IOException;

public interface ReadingSupplier<T> {

  /**
   * Gets a result.
   *
   * @return a result
   */
  T get() throws IOException;

}
