package io.mapsmessaging.devices.gpio;

import java.io.IOException;
import java.util.Map;

public interface InterruptFactory {
  InterruptPin allocateInterruptPin(Map<String, String> config) throws IOException;

  void deallocateInterruptPin(InterruptPin pin) throws IOException;
}
