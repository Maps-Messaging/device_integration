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

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.gpio.PiPinManagement;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import lombok.Getter;

import java.io.IOException;
import java.util.BitSet;

public class ShiftRegisterDevice implements Device {


  private static final String NAME = "Shift-Register";

  private final BitSet individualBits;
  private final BaseDigitalOutput dataPort;
  private final BaseDigitalOutput clockPort;
  private final BaseDigitalOutput latchPort;
  private final BaseDigitalOutput clearPort;
  @Getter
  private final int totalBits;

  public ShiftRegisterDevice(int numberOfRegisters, int sizeOfRegister, int dataPin, int clockPin, int latchPin, int clearPin) throws IOException {
    totalBits = sizeOfRegister * numberOfRegisters;
    individualBits = new BitSet(totalBits);

    PiPinManagement pinManagement = DeviceBusManager.getInstance().getPinManagement();
    dataPort = pinManagement.allocateOutPin(NAME + "-data", "data", dataPin, false);
    clockPort = pinManagement.allocateOutPin(NAME + "-clock", "clock", clockPin, false);
    latchPort = pinManagement.allocateOutPin(NAME + "-latch", "latch", latchPin, false);
    if (clearPin > 0) {
      clearPort = pinManagement.allocateOutPin(NAME + "-clear", "clear", clearPin, false);
      clearPort.setDown();
    } else {
      clearPort = null;
    }
    dataPort.setDown();
    clockPort.setDown();
    latchPort.setDown();
    clearAll();
  }

  public void setBit(int bit) {
    individualBits.set(bit, true);
  }

  public void clearBit(int bit) {
    individualBits.set(bit, false);
  }

  public void clearAll() throws IOException {
    individualBits.clear();
    if (clearPort != null) {
      clearPort.setDown();
      delay(10);
      clearPort.setUp();
    }
    write();
  }

  public void setAll() throws IOException {
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

  public void write() throws IOException {
    latchPort.setDown();
    boolean dataState = false;
    for (int x = totalBits - 1; x >= 0; x--) {
      delay(10);
      if (individualBits.get(x)) {
        if (!dataState) {
          dataPort.setUp();
          dataState = true;
        } else {
          dataPort.setDown();
          dataState = false;
        }
      }
      delay(10);
      clockPort.setUp();
      delay(10);
      clockPort.setDown();
    }
    dataPort.setDown();
    latchPort.setUp();
    delay(10);
    latchPort.setDown();
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
