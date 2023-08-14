/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.gpio.pin;

import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GpioDigitalInput extends BaseDigitalInput {

  private final Gpio gpio;
  private final List<DigitalStateChangeListener> listenerList;

  public GpioDigitalInput(String id, String name, Gpio gpio, int pin, boolean pullUp) throws IOException {
    super(pin, id, name);
    listenerList = new CopyOnWriteArrayList<>();
    this.gpio = gpio;
    gpio.setInput(pin);
    if (pullUp) {
      gpio.enablePullUp(pin);
    } else {
      gpio.disablePullUp(pin);
    }
  }

  @Override
  public DigitalState getState() throws IOException {
    if (gpio.isSet(pin)) {
      return DigitalState.HIGH;
    }
    return DigitalState.LOW;
  }

  @Override
  public void addListener(DigitalStateChangeListener... var1) throws IOException {
    gpio.enableInterrupt(pin);
    listenerList.addAll(Arrays.asList(var1));
  }

  @Override
  public void removeListener(DigitalStateChangeListener... var1) throws IOException {
    listenerList.removeAll(Arrays.asList(var1));
    if(listenerList.isEmpty()){
      gpio.disableInterrupt(pin);
    }
  }

  @Override
  public String toString() {
    return id + " " + name;
  }

  public void stateChange() throws IOException {
    DigitalStateChangeEvent event = new DigitalStateChangeEvent(null, getState());
    for(DigitalStateChangeListener listener: listenerList){
      listener.onDigitalStateChange(event);
    }
  }
}

