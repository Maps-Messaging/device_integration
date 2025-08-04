/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.PowerModeData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.LowPowerBandwidth;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.PowerMode;

import java.io.IOException;

public class PowerModeRegister extends SingleByteRegister {

  public PowerModeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x11, "Power_Mode");
  }

  public LowPowerBandwidth getLowPowerBandwidth() throws IOException {
    reload();
    int val = (registerValue & 0b11110) >> 1;
    for (LowPowerBandwidth odr : LowPowerBandwidth.values()) {
      if (val <= odr.getEnd() && val >= odr.getStart()) {
        return odr;
      }
    }
    return LowPowerBandwidth.HERTZ_1_95; // Default
  }

  public void setLowPowerBandwidth(LowPowerBandwidth bandwidth) throws IOException {
    registerValue = (byte) ((registerValue & 0b11000001) | (bandwidth.getStart() << 1));
    sensor.write(address, registerValue);
  }

  public PowerMode getPowerMode() {
    int val = registerValue >> 6;
    for (PowerMode mode : PowerMode.values()) {
      if (mode.ordinal() == val) {
        return mode;
      }
    }
    return PowerMode.UNKNOWN;
  }

  public void setPowerMode(PowerMode mode) throws IOException {
    super.setControlRegister(0b00011110, mode.ordinal() << 6);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new PowerModeData(getLowPowerBandwidth(), getPowerMode());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof PowerModeData) {
      PowerModeData data = (PowerModeData) input;
      setLowPowerBandwidth(data.getLowPowerBandwidth());
      setPowerMode(data.getPowerMode());
      return true;
    }
    return false;
  }

}
