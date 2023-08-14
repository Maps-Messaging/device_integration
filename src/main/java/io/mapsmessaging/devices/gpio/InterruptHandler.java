package io.mapsmessaging.devices.gpio;

import java.io.IOException;

public interface InterruptHandler {
  void interruptFired() throws IOException;
}
