package io.mapsmessaging.devices.gpio;

import com.pi4j.context.Context;

import java.io.IOException;
import java.util.Map;

public class InterruptFactory {

  private final Context context;

  public InterruptFactory(Context context){
    this.context = context;
  }

  public InterruptPin allocateInterruptPin(Map<String, String> config) throws IOException {

    return new InterruptPin();
  }

  public void deallocateInterruptPin(InterruptPin pin) throws IOException{

  }

}
