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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.ControlData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.ClockFrequency;

import java.io.IOException;

public class ControlRegister extends SingleByteRegister {

  private static final int ENABLE_OSC   = 0b10000000;
  private static final int ENABLE_BB_SQ = 0b01000000;
  private static final int CONV         = 0b00100000;
  private static final int CLOCK_FREQ   = 0b00011000;
  private static final int INT_ENABLE   = 0b00000100;
  private static final int ALARM2_INT   = 0b00000010;
  private static final int ALARM1_INT   = 0b00000001;


  public ControlRegister(I2CDevice device) throws IOException {
    super(device, 0xE, "CONTROL");
  }

  public boolean isOscillatorEnabled() {
    return (registerValue & ENABLE_OSC) != 0;
  }

  public void setOscillatorEnabled(boolean enabled) throws IOException {
    setControlRegister(~ENABLE_OSC, enabled?ENABLE_OSC:0);
  }

  public boolean isSquareWaveEnabled() {
    return (registerValue & ENABLE_BB_SQ) != 0;
  }

  public void setSquareWaveEnabled(boolean enabled) throws IOException {
    setControlRegister(~ENABLE_BB_SQ, enabled?ENABLE_BB_SQ:0);
  }

  public boolean isConvertTemperatureEnabled() {
    return (registerValue & CONV) != 0;
  }

  public void setConvertTemperature(boolean enabled) throws IOException {
    setControlRegister(~CONV, enabled?CONV:0);
  }

  public ClockFrequency getSquareWaveFrequency() {
    int frequencyBits = (registerValue &CLOCK_FREQ)>>3;
    return ClockFrequency.values()[frequencyBits];
  }

  public void setSquareWaveFrequency(ClockFrequency frequency) throws IOException {
    setControlRegister(~CLOCK_FREQ, frequency.ordinal()<<3);
  }

  public boolean isSquareWaveInterruptEnabled() {
    return (registerValue & INT_ENABLE) != 0;
  }

  public void setSquareWaveInterruptEnabled(boolean enabled) throws IOException {
    setControlRegister(~INT_ENABLE, enabled?INT_ENABLE:0);
  }

  public boolean isAlarm1InterruptEnabled() throws IOException {
    reload();
    return (registerValue & ALARM1_INT) != 0;
  }

  public void setAlarm1InterruptEnabled(boolean enabled) throws IOException {
    setControlRegister(~ALARM1_INT, enabled?ALARM1_INT:0);
  }

  public boolean isAlarm2InterruptEnabled() throws IOException {
    reload();
    return (registerValue & ALARM2_INT) != 0;
  }

  public void setAlarm2InterruptEnabled(boolean enabled) throws IOException {
    setControlRegister(~ALARM2_INT, enabled?ALARM2_INT:0);
  }

  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof ControlData) {
      ControlData data = (ControlData) input;
      setOscillatorEnabled(data.isOscillatorEnabled());
      setSquareWaveEnabled(data.isSquareWaveEnabled());
      setConvertTemperature(data.isConvertTemperatureEnabled());
      setSquareWaveFrequency(data.getSquareWaveFrequency());
      setSquareWaveInterruptEnabled(data.isSquareWaveInterruptEnabled());
      setAlarm1InterruptEnabled(data.isAlarm1InterruptEnabled());
      setAlarm2InterruptEnabled(data.isAlarm2InterruptEnabled());
      return true;
    }
    return false;
  }

  @Override
  public AbstractRegisterData toData() throws IOException {
    return new ControlData(
        isOscillatorEnabled(),
        isSquareWaveEnabled(),
        isConvertTemperatureEnabled(),
        getSquareWaveFrequency(),
        isSquareWaveInterruptEnabled(),
        isAlarm1InterruptEnabled(),
        isAlarm2InterruptEnabled());
  }
}

