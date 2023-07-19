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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.Control1Data;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;

import java.io.IOException;

public class Control1 extends SingleByteRegister {

  private static final byte CONTROL_REGISTER1 = 0x20;

  private static final byte POWER_DOWN = (byte) (0b10000000);
  private static final byte POWER_RATE = 0b01110000; // Do not keep the power down bit
  private static final byte INTERRUPT_ENABLE = 0b00001000;
  private static final byte BLOCK_DATA_UPDATE = 0b00000100;
  private static final byte RESET_AUTO_ZERO = 0b00000010;

  public Control1(I2CDevice sensor) throws IOException {
    super(sensor, CONTROL_REGISTER1, "CTRL_REG1");
  }

  public boolean getPowerDownMode() {
    return (registerValue & POWER_DOWN) != 0;
  }

  public void setPowerDownMode(boolean flag) throws IOException {
    int value = flag ? POWER_DOWN : 0;
    setControlRegister((byte) ~POWER_DOWN, value);
  }

  public DataRate getDataRate() {
    int rateVal = ((registerValue & POWER_RATE) >> 4);
    for (DataRate rate : DataRate.values()) {
      if (rate.getMask() == rateVal) {
        return rate;
      }
    }
    return DataRate.RATE_ONE_SHOT;
  }

  public void setDataRate(DataRate rate) throws IOException {
    setControlRegister(~POWER_RATE, (rate.getMask() << 4));
  }

  public boolean isInterruptGenerationEnabled() {
    return (registerValue & INTERRUPT_ENABLE) != 0;
  }

  public void setInterruptGenerationEnabled(boolean flag) throws IOException {
    int value = flag ? INTERRUPT_ENABLE : 0;
    setControlRegister(~INTERRUPT_ENABLE, value);
  }

  public boolean isBlockUpdateSet() {
    return (registerValue & BLOCK_DATA_UPDATE) != 0;
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    int value = flag ? BLOCK_DATA_UPDATE : 0;
    setControlRegister(~BLOCK_DATA_UPDATE, value);
  }

  public void resetAutoZero(boolean flag) throws IOException {
    int value = flag ? RESET_AUTO_ZERO : 0;
    setControlRegister(~RESET_AUTO_ZERO, value);
  }

  @Override
  public AbstractRegisterData toData(){
    return new Control1Data(getPowerDownMode(), getDataRate(), isInterruptGenerationEnabled(), isBlockUpdateSet());
  }

  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if(input instanceof Control1Data) {
      Control1Data data = (Control1Data)input;
      setDataRate(data.getDataRate());
      setInterruptGenerationEnabled(data.isInterruptGenerationEnabled());
      setBlockUpdate(data.isBlockUpdateSet());
      setPowerDownMode(data.isPowerDownMode());
      return true;
    }
    return false;
  }
}
