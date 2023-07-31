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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.TunCapData;

import java.io.IOException;

public class TunCapRegister extends SingleByteRegister {

  private static final int TUN_CAP_CAP_BITS = 0;
  private static final int TUN_CAP_DISP_TRCO = 0b01000000;
  private static final int TUN_CAP_DISP_SRCO = 0b10000000;

  public TunCapRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x08, "Tune Capacitor");
  }

  public int getTuningCap() {
    return registerValue & 0x0F;
  }

  public void setTuningCap(int cap) throws IOException {
    registerValue &= ~((0x0F) << TUN_CAP_CAP_BITS);
    registerValue |= cap & ((0x0F) << TUN_CAP_CAP_BITS);
    sensor.write(address, registerValue);
    sensor.delay(200);
  }

  public boolean isDispTRCOEnabled() {
    return ((registerValue & 0xff) & TUN_CAP_DISP_TRCO) != 0;
  }

  public void setDispTRCOEnabled(boolean enabled) throws IOException {
    if (enabled) {
      registerValue |= TUN_CAP_DISP_TRCO;
    } else {
      registerValue &= ~(TUN_CAP_DISP_TRCO);
    }
    sensor.write(address, registerValue);
    sensor.delay(200);
  }

  public boolean isDispSRCOEnabled() {
    return ((registerValue & 0xff) & (TUN_CAP_DISP_SRCO)) != 0;
  }

  public void setDispSRCOEnabled(boolean enabled) throws IOException {
    if (enabled) {
      registerValue |= (TUN_CAP_DISP_SRCO);
    } else {
      registerValue &= ~(TUN_CAP_DISP_SRCO);
    }
    sensor.write(address, registerValue);
    sensor.delay(200);
  }

  @Override
  public RegisterData toData() throws IOException {
    int tuningCap = getTuningCap();
    boolean dispTRCOEnabled = isDispTRCOEnabled();
    boolean dispSRCOEnabled = isDispSRCOEnabled();
    return new TunCapData(tuningCap, dispTRCOEnabled, dispSRCOEnabled);
  }

  // Method to set Tun_Cap_Register data from TunCapData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof TunCapData) {
      TunCapData data = (TunCapData) input;
      setTuningCap(data.getTuningCap());
      setDispTRCOEnabled(data.isDispTRCOEnabled());
      setDispSRCOEnabled(data.isDispSRCOEnabled());
      return true;
    }
    return false;
  }
}