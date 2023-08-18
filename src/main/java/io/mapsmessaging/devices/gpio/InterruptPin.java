package io.mapsmessaging.devices.gpio;

import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InterruptPin implements DigitalStateChangeListener {

  private final BaseDigitalInput pin;
  private final List<InterruptListener> listeners;

  public InterruptPin(BaseDigitalInput pin){
    this.pin = pin;
    listeners = new CopyOnWriteArrayList<>();
    pin.addListener(this);
  }

  public void addListener(InterruptListener listener){
    listeners.add(listener);
  }

  public void removeListener(InterruptListener listener){
    listeners.remove(listener);
  }

  public void close() {
    listeners.clear();
  }

  @Override
  public void onDigitalStateChange(DigitalStateChangeEvent digitalStateChangeEvent) {
    for(InterruptListener listener:listeners){
      listener.interrupt(this, digitalStateChangeEvent.state());
    }
  }
}
