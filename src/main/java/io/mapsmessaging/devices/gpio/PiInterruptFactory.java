package io.mapsmessaging.devices.gpio;

import com.pi4j.context.Context;

import java.io.IOException;
import java.util.Map;

public class PiInterruptFactory implements InterruptFactory {

  private final Pi4JPinManagement pinManagement;

  public PiInterruptFactory(Context context){
    pinManagement= new Pi4JPinManagement(context);
  }

  @Override
  public InterruptPin allocateInterruptPin(Map<String, String> config) {
    return new InterruptPin(pinManagement.allocateInPin(config));
  }

  @Override
  public void deallocateInterruptPin(InterruptPin pin) throws IOException{
    pin.close();
  }

}
