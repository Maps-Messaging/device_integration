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
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.AfeData;

import java.io.IOException;

public class AfeRegister extends SingleByteRegister {
  private static final int AFE_GAIN_ADDR = 0x00;
  private static final byte AFE_GAIN_PD_BIT = 0;
  private static final byte AFE_GAIN_BOOST_BITS = 1;

  public AfeRegister(I2CDevice sensor) throws IOException {
    super(sensor, AFE_GAIN_ADDR, "AFE Gain");
    reload();
  }

  public boolean isPowerDown() {
    return ((registerValue & 0xff) & (1 << AFE_GAIN_PD_BIT)) != 0;
  }

  public void setPowerDown(boolean powerDown) throws IOException {
    byte value = registerValue;
    if (powerDown) {
      value |= (1 << AFE_GAIN_PD_BIT);
    } else {
      value &= ~(1 << AFE_GAIN_PD_BIT);
    }
    sensor.write(AFE_GAIN_ADDR, value);
  }

  public int getGainBoost() {
    return (registerValue >> AFE_GAIN_BOOST_BITS) & 0x1F;
  }

  public void setGainBoost(int gainBoost) throws IOException {
    registerValue &= ~((0x1F) << AFE_GAIN_BOOST_BITS);
    registerValue |= (gainBoost << AFE_GAIN_BOOST_BITS) & ((0x1F) << AFE_GAIN_BOOST_BITS);
    sensor.write(AFE_GAIN_ADDR, registerValue);
  }

  public String getBoostName() {
    int gain = getGainBoost();
    if (gain == 0b10010) {
      return "Indoor";
    }
    if (gain == 0b01110) {
      return "Outdoor";
    }
    return "Custom";
  }

  public void setIndoor() throws IOException {
    setGainBoost(0b10010);
  }

  public void setOutdoor() throws IOException {
    setGainBoost(0b01110);
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean powerDown = isPowerDown();
    int gainBoost = getGainBoost();
    return new AfeData(powerDown, gainBoost, getBoostName());
  }

  // Method to set AfeRegister data from AfeData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof AfeData) {
      AfeData data = (AfeData) input;
      setPowerDown(data.isPowerDown());
      setGainBoost(data.getGainBoost());
      return true;
    }
    return false;
  }

}
