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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class Tun_Cap_Register extends Register {

  private static final int TUN_CAP_CAP_BITS = 0;
  private static final int TUN_CAP_DISP_TRCO_BIT = 6;
  private static final int TUN_CAP_DISP_SRCO_BIT = 7;

  public Tun_Cap_Register(I2CDevice sensor) {
    super(sensor, 0x08);
  }

  public int getTuningCap() throws IOException {
    return registerValue & 0x0F;
  }

  public void setTuningCap(int cap) throws IOException {
    registerValue &= ~((0x0F) << TUN_CAP_CAP_BITS);
    registerValue |= cap & ((0x0F) << TUN_CAP_CAP_BITS);
    sensor.write(address, registerValue);
  }

  public boolean isDispTRCOEnabled() throws IOException {
    return (registerValue & (1 << TUN_CAP_DISP_TRCO_BIT)) != 0;
  }

  public void setDispTRCOEnabled(boolean enabled) throws IOException {
    if (enabled) {
      registerValue |= (1 << TUN_CAP_DISP_TRCO_BIT);
    } else {
      registerValue &= ~(1 << TUN_CAP_DISP_TRCO_BIT);
    }
    sensor.write(address, registerValue);
  }

  public boolean isDispSRCOEnabled() throws IOException {
    return (registerValue & (1 << TUN_CAP_DISP_SRCO_BIT)) != 0;
  }

  public void setDispSRCOEnabled(boolean enabled) throws IOException {
    if (enabled) {
      registerValue |= (1 << TUN_CAP_DISP_SRCO_BIT);
    } else {
      registerValue &= ~(1 << TUN_CAP_DISP_SRCO_BIT);
    }
    sensor.write(address, registerValue);
  }
}