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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.ResolutionData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.PressureAverage;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.TemperatureAverage;

import java.io.IOException;

public class ResolutionRegister extends SingleByteRegister {

  private static final byte RESOLUTION_ADDRESS = 0x10;
  private static final byte AVE_PRESSURE_MASK = 0b00000011;
  private static final byte AVE_TEMPERATURE_MASK = 0b00001100;

  public ResolutionRegister(I2CDevice sensor) throws IOException {
    super(sensor, RESOLUTION_ADDRESS, "RES_CONF");
  }

  public PressureAverage getPressureAverage() {
    int rateVal = ((registerValue & AVE_PRESSURE_MASK));
    for (PressureAverage pressureAverage : PressureAverage.values()) {
      if (pressureAverage.getMask() == rateVal) {
        return pressureAverage;
      }
    }
    return PressureAverage.AVERAGE_8;
  }

  public void setPressureAverage(PressureAverage ave) throws IOException {
    setControlRegister(~AVE_PRESSURE_MASK, (ave.getMask()));
  }

  public TemperatureAverage getTemperatureAverage() {
    int rateVal = ((registerValue & AVE_TEMPERATURE_MASK) >> 2);
    for (TemperatureAverage average : TemperatureAverage.values()) {
      if (average.getMask() == rateVal) {
        return average;
      }
    }
    return TemperatureAverage.AVERAGE_8;
  }


  public void setTemperatureAverage(TemperatureAverage ave) throws IOException {
    setControlRegister(~AVE_TEMPERATURE_MASK, (ave.getMask() << 2));
  }

  public RegisterData toData() {
    return new ResolutionData(getPressureAverage(), getTemperatureAverage());
  }

  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ResolutionData) {
      ResolutionData data = (ResolutionData) input;
      setPressureAverage(data.getPressureAverage());
      setTemperatureAverage(data.getTemperatureAverage());
      return true;
    }
    return false;
  }


}