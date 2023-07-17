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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientBlocking;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientMode;

import java.io.IOException;

public class OrientHyRegister extends SingleByteRegister {
  private static final byte ORIENT_BLOCKING_MASK = (byte) 0b00001100;
  private static final byte ORIENT_MODE_MASK = (byte) 0b00000011;
  private static final byte ORIENT_HYST_MASK = (byte) 0b01110000;


  public OrientHyRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x2C, "Orient_Hy");
  }

  public int getOrientHysteresis() throws IOException {
    reload();
    return ((registerValue & 0xff) & ORIENT_HYST_MASK) >> 4;
  }

  public void setOrientHysteresis(int hysteresis) throws IOException {
    setControlRegister(~ORIENT_HYST_MASK, (byte) (hysteresis << 4));
  }


  public OrientBlocking getOrientBlocking() throws IOException {
    reload();
    int maskedValue = (registerValue & ORIENT_BLOCKING_MASK) >> 2;
    return OrientBlocking.fromValue(maskedValue);
  }

  public void setOrientBlocking(OrientBlocking blocking) throws IOException {
    int maskedValue = (blocking.getValue() << 2) & (ORIENT_BLOCKING_MASK & 0xff);
    setControlRegister(~ORIENT_BLOCKING_MASK, maskedValue);
  }

  public OrientMode getOrientMode() throws IOException {
    reload();
    int maskedValue = (registerValue & ORIENT_MODE_MASK);
    return OrientMode.fromValue(maskedValue);
  }

  public void setOrientMode(OrientMode mode) throws IOException {
    int maskedValue = (mode.getValue()) & ORIENT_MODE_MASK;
    setControlRegister(~ORIENT_MODE_MASK, maskedValue);
  }
}

