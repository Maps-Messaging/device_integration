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

package io.mapsmessaging.devices.direct.shift;

import com.pi4j.io.gpio.digital.DigitalOutput;
import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.direct.PinManagement;

import java.util.BitSet;

public class ShiftRegisterDevice implements Device {


  private static final String NAME = "Shift-Register";

  private final BitSet individualBits;
  private final DigitalOutput dataPort;
  private final DigitalOutput clockPort;
  private final DigitalOutput latchPort;
  private final DigitalOutput clearPort;
  private final int totalBits;

  public ShiftRegisterDevice(int numberOfRegisters, int sizeOfRegister, int dataPin, int clockPin, int latchPin, int clearPin) {
    totalBits = sizeOfRegister * numberOfRegisters;
    individualBits = new BitSet(totalBits);

    PinManagement pinManagement = DeviceBusManager.getInstance().getPinManagement();
    dataPort = pinManagement.allocateGPIOPin(NAME+"-data","data", dataPin, "DOWN");
    clockPort = pinManagement.allocateGPIOPin(NAME+"-clock","clock", clockPin, "DOWN");
    latchPort = pinManagement.allocateGPIOPin(NAME+"-latch","latch", latchPin, "DOWN");
    if(clearPin > 0) {
      clearPort = pinManagement.allocateGPIOPin(NAME + "-clear", "clear", clearPin, "DOWN");
      clearPort.low();
    }
    else{
      clearPort = null;
    }
    dataPort.low();
    clockPort.low();
    latchPort.low();
    clearAll();
  }

  public int getTotalBits() {
    return totalBits;
  }

  public void setBit(int bit) {
    individualBits.set(bit, true);
  }

  public void clearBit(int bit) {
    individualBits.set(bit, false);
  }

  public void clearAll() {
    individualBits.clear();
    if (clearPort != null) {
      clearPort.low();
      delay(10);
      clearPort.high();
    }
    write();
  }

  public void setAll() {
    individualBits.set(0, totalBits);
    write();
  }

  public void setLong(long val) {
    int len = Math.min(totalBits, 64);
    long bitMap = 1;
    for (int x = 0; x < len; x++) {
      individualBits.set(x, (val & bitMap) != 0);
      bitMap = bitMap << 1;
    }
  }

  public void write() {
    latchPort.low();
    boolean dataState = false;
    for (int x = totalBits - 1; x >= 0; x--) {
      delay(10);
      if (individualBits.get(x)) {
        if (!dataState) {
          dataPort.high();
          dataState = true;
        }
        else {
          dataPort.low();
          dataState = false;
        }
      }
      delay(10);
      clockPort.high();
      delay(10);
      clockPort.low();
    }
    dataPort.low();
    latchPort.high();
    delay(10);
    latchPort.low();
  }

  @Override
  public String getName() {
    return "Shift Register";
  }

  @Override
  public String getDescription() {
    return "SNx4HC595 8-Bit Shift Registers";
  }
}
