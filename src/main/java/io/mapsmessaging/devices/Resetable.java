package io.mapsmessaging.devices;

import java.io.IOException;

public interface Resetable {

  void reset()  throws IOException;
  void softReset() throws IOException;
}
