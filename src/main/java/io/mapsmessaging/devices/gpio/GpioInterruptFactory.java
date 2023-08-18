package io.mapsmessaging.devices.gpio;

import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;

import java.io.IOException;
import java.util.Map;

public class GpioInterruptFactory  implements InterruptFactory {

  private final GpioExtensionPinManagement pinManagement;

  public GpioInterruptFactory(Gpio gpio){
    this(gpio, null);
  }

  public GpioInterruptFactory(Gpio gpio, BaseDigitalInput interruptInput) {
    if(interruptInput == null) {
      pinManagement = new GpioExtensionPinManagement(gpio);
    }
    else{
      pinManagement = new GpioExtensionPinManagement(gpio, interruptInput);
    }
  }

  @Override
  public InterruptPin allocateInterruptPin(Map<String, String> config) throws IOException {
    return new InterruptPin(pinManagement.allocateInPin(config));
  }

  @Override
  public void deallocateInterruptPin(InterruptPin pin) throws IOException {
    pin.close();
  }
}
