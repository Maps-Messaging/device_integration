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

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.Control1Data;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.DataRate;

import java.io.IOException;

public class Control1Register extends SingleByteRegister {

  private static final int DATA_RATE = 0b01110000;
  private static final int LOW_PASS = 0b00001000;
  private static final int LOW_PASS_CFG = 0b00000100;
  private static final int BLOCK_UPDATE = 0b00000010;

  public Control1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x10, "CTRL_REG1");
    reload();
  }

  public DataRate getDataRate() {
    int ctl1 = (registerValue & DATA_RATE);
    for (DataRate rate : DataRate.values()) {
      if (rate.getMask() == ctl1) {
        return rate;
      }
    }
    return DataRate.RATE_ONE_SHOT;
  }

  public void setDataRate(DataRate rate) throws IOException {
    setControlRegister(~DATA_RATE, (rate.getMask()));
  }

  public void setLowPassFilter(boolean flag) throws IOException {
    setControlRegister(~LOW_PASS, flag ? LOW_PASS : 0);
  }

  public boolean isLowPassFilterSet() {
    return (registerValue & LOW_PASS) != 0;
  }

  public void setLowPassFilterConfig(boolean flag) throws IOException {
    setControlRegister(~LOW_PASS_CFG, flag ? LOW_PASS_CFG : 0);
  }

  public boolean isLowPassFilterConfigSet() {
    return (registerValue & LOW_PASS_CFG) != 0;
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    setControlRegister(~BLOCK_UPDATE, flag ? BLOCK_UPDATE : 0);
  }

  public boolean isBlockUpdateSet() {
    return (registerValue & BLOCK_UPDATE) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    DataRate dataRate = getDataRate();
    boolean lowPassFilter = isLowPassFilterSet();
    boolean lowPassFilterConfig = isLowPassFilterConfigSet();
    boolean blockUpdate = isBlockUpdateSet();
    return new Control1Data(dataRate, lowPassFilter, lowPassFilterConfig, blockUpdate);
  }

  // Method to set Control1Register data from Control1Data
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Control1Data data) {
      setDataRate(data.getDataRate());
      setLowPassFilter(data.isLowPassFilter());
      setLowPassFilterConfig(data.isLowPassFilterConfig());
      setBlockUpdate(data.isBlockUpdate());
      return true;
    }
    return false;
  }

}
