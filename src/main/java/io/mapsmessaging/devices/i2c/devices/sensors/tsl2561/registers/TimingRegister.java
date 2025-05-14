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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.TimingData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;

import java.io.IOException;

public class TimingRegister extends SingleByteRegister {

  private static final byte GAIN_MASK = 0b00010000;
  private static final byte MANUAL_MASK = 0b00001000;
  private static final byte INTEGRATION_MASK = 0b00000011;


  public TimingRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x81, "Timing");
    reload();
  }

  public boolean getManual() {
    return (registerValue & MANUAL_MASK) != 0;
  }

  public void setManual(boolean flag) throws IOException {
    if (flag) {
      registerValue |= MANUAL_MASK;
    } else {
      registerValue &= ~MANUAL_MASK;
    }
    sensor.write(address, registerValue);
  }

  public boolean getHighGain() {
    return (registerValue & GAIN_MASK) != 0;
  }

  public void setHighGain(boolean flag) throws IOException {
    if (flag) {
      registerValue |= GAIN_MASK;
    } else {
      registerValue &= ~GAIN_MASK;
    }
    sensor.write(address, registerValue);
  }

  public IntegrationTime getIntegrationTime() {
    byte val = (byte) (registerValue & INTEGRATION_MASK);
    for (IntegrationTime time : IntegrationTime.values()) {
      if (time.getMask() == val) {
        return time;
      }
    }
    return IntegrationTime.MANUAL;
  }

  public void setIntegrationTime(IntegrationTime times) throws IOException {
    byte mask = times.getMask();
    super.setControlRegister(~INTEGRATION_MASK, mask);
    sensor.delay(500);
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean manual = getManual();
    boolean highGain = getHighGain();
    IntegrationTime integrationTime = getIntegrationTime();
    return new TimingData(manual, highGain, integrationTime);
  }

  // Method to set TimingRegister data from TimingData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof TimingData) {
      TimingData data = (TimingData) input;
      setManual(data.isManual());
      setHighGain(data.isHighGain());
      setIntegrationTime(data.getIntegrationTime());
      return true;
    }
    return false;
  }

}