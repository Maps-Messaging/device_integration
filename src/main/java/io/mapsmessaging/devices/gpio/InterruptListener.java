package io.mapsmessaging.devices.gpio;

import com.pi4j.io.gpio.digital.DigitalState;

public interface InterruptListener {

  void interrupt(InterruptPin pin, DigitalState state);

}
