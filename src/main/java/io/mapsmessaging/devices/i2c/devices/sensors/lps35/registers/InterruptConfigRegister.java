/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.InterruptConfigData;

import java.io.IOException;

public class InterruptConfigRegister extends SingleByteRegister {

  private static final int AUTO_RIFP = 0b10000000;
  private static final int RESET_ARP = 0b01000000;
  private static final int AUTO_ZERO = 0b00100000;
  private static final int RESET_AZ = 0b00010000;
  private static final int DIFF_EN = 0b00001000;
  private static final int LIR = 0b00000100;
  private static final int PLE = 0b00000010;
  private static final int PHE = 0b00000001;

  public InterruptConfigRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x0B, "INTERRUPT_CFG");
    reload();
  }

  //region Interrupt Config Register
  public void enableAutoRifp(boolean flag) throws IOException {
    setControlRegister(~AUTO_RIFP, flag ? AUTO_RIFP : 0);
  }

  public boolean isAutoRifpEnabled() {
    return (registerValue & AUTO_RIFP) != 0;
  }

  public void resetAutoRifp() throws IOException {
    setControlRegister(~RESET_ARP, RESET_ARP);
  }

  public void enableAutoZero(boolean flag) throws IOException {
    setControlRegister(~AUTO_ZERO, flag ? AUTO_ZERO : 0);
  }

  public boolean isAutoZeroEnabled() {
    return (registerValue & AUTO_ZERO) != 0;
  }

  public void resetAutoZero() throws IOException {
    setControlRegister(~RESET_AZ, RESET_AZ);
  }

  public void enableInterrupt(boolean flag) throws IOException {
    setControlRegister(~DIFF_EN, flag ? DIFF_EN : 0);
  }

  public boolean isInterruptEnabled() {
    return (registerValue & DIFF_EN) != 0;
  }

  public void latchInterruptToSource(boolean flag) throws IOException {
    setControlRegister(~LIR, flag ? LIR : 0);
  }

  public boolean isLatchInterruptToSource() {
    return (registerValue & LIR) != 0;
  }

  public void latchInterruptToPressureLow(boolean flag) throws IOException {
    setControlRegister(~PLE, flag ? PLE : 0);
  }

  public boolean isLatchInterruptToPressureLow() {
    return (registerValue & PLE) != 0;
  }

  public void latchInterruptToPressureHigh(boolean flag) throws IOException {
    setControlRegister(~PHE, flag ? PHE : 0);
  }

  public boolean isLatchInterruptToPressureHigh() {
    return (registerValue & PHE) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean autoRifpEnabled = isAutoRifpEnabled();
    boolean autoZeroEnabled = isAutoZeroEnabled();
    boolean interruptEnabled = isInterruptEnabled();
    boolean latchInterruptToSource = isLatchInterruptToSource();
    boolean latchInterruptToPressureLow = isLatchInterruptToPressureLow();
    boolean latchInterruptToPressureHigh = isLatchInterruptToPressureHigh();
    return new InterruptConfigData(autoRifpEnabled, autoZeroEnabled, interruptEnabled, latchInterruptToSource, latchInterruptToPressureLow, latchInterruptToPressureHigh);
  }

  // Method to set InterruptConfigRegister data from InterruptConfigData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptConfigData) {
      InterruptConfigData data = (InterruptConfigData) input;
      enableAutoRifp(data.isAutoRifpEnabled());
      enableAutoZero(data.isAutoZeroEnabled());
      enableInterrupt(data.isInterruptEnabled());
      latchInterruptToSource(data.isLatchInterruptToSource());
      latchInterruptToPressureLow(data.isLatchInterruptToPressureLow());
      latchInterruptToPressureHigh(data.isLatchInterruptToPressureHigh());
      return true;
    }
    return false;
  }
}
