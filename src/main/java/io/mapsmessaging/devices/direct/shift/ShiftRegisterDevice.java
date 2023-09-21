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
import io.mapsmessaging.devices.gpio.PinManagement;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import lombok.Getter;

import java.io.IOException;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShiftRegisterDevice implements Device {

  private static final int BIT_DELAY_TIME_MS = 1;

  private static final String NAME = "Shift-Register";

  private final BitSet individualBits;
  private final BaseDigitalOutput dataPort;
  private final BaseDigitalOutput clockPort;
  private final BaseDigitalOutput latchPort;
  private final BaseDigitalOutput clearPort;
  @Getter
  private final int totalBits;

  public ShiftRegisterDevice(
      int numberOfRegisters,
      int sizeOfRegister,
      int dataPin,
      int clockPin,
      int latchPin,
      int clearPin,
      PinManagement pinManagement
  ) throws IOException {
    totalBits = sizeOfRegister * numberOfRegisters;
    individualBits = new BitSet(totalBits);
    dataPort = pinManagement.allocateOutPin(buildConfig(NAME + "-data", "data", dataPin));
    clockPort = pinManagement.allocateOutPin(buildConfig(NAME + "-clock", "clock", clockPin));
    latchPort = pinManagement.allocateOutPin(buildConfig(NAME + "-latch", "latch", latchPin));
    if (clearPin > 0) {
      clearPort = pinManagement.allocateOutPin(buildConfig(NAME + "-clear", "clear", clearPin));
      clearPort.setLow();
    } else {
      clearPort = null;
    }
    dataPort.setLow();
    clockPort.setLow();
    latchPort.setLow();
    clearAll();
  }

  private Map<String, String> buildConfig(String id, String name, int pin) {
    Map<String, String> config = new LinkedHashMap<>();
    config.put("id",id);
    config.put("name", name);
    config.put("pin", ""+pin);

    return config;
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
      clearPort.setLow();
      delay(10);
      clearPort.setHigh();
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
    sendData();
    sequenceLatch();
  }

  private void sendData() throws IOException {
    for (int x = totalBits - 1; x >= 0; x--) {
      if (individualBits.get(x)) {
        dataPort.setHigh();
      } else {
        dataPort.setLow();
      }
      sequenceClock();
    }
    dataPort.setLow();
  }

  private void sequenceLatch() throws IOException{
    latchPort.setHigh();
    delay(BIT_DELAY_TIME_MS);
    latchPort.setLow();
  }

  private void sequenceClock() throws IOException {
    delay(BIT_DELAY_TIME_MS);
    clockPort.setHigh();
    delay(BIT_DELAY_TIME_MS);
    clockPort.setLow();
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
