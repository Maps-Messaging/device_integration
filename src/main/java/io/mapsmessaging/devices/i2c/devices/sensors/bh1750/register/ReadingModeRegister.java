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

package io.mapsmessaging.devices.i2c.devices.sensors.bh1750.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.data.ReadingModeData;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.ResolutionMode;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.SensorReadingMode;
import lombok.Getter;

import java.io.IOException;

@Getter
public class ReadingModeRegister extends Register {

  private ResolutionMode resolutionMode;

  @Getter
  private SensorReadingMode sensorReading;


  public ReadingModeRegister(I2CDevice sensor, int address, String name) {
    super(sensor, address, name);
  }

  public void setSensorReading(SensorReadingMode reading) throws IOException {
    sensorReading = reading;
    sensor.write(resolutionMode.getMask() | sensorReading.getMask());
  }


  public void setResolutionMode(ResolutionMode mode) throws IOException {
    resolutionMode = mode;
    int val = 0;
    if (resolutionMode != null) val = resolutionMode.getMask();
    if (sensorReading != null) val = val | sensorReading.getMask();
    sensor.write(val);
  }

  @Override
  public RegisterData toData() {
    return new ReadingModeData(resolutionMode, sensorReading);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ReadingModeData) {
      ReadingModeData data = (ReadingModeData) input;
      setResolutionMode(data.getResolutionMode());
      setSensorReading(data.getSensorReading());
      return true;
    }
    return false;
  }

  @Override
  protected void reload() throws IOException {

  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {

  }

  @Override
  public String toString(int length) {
    int val = 0;
    if (resolutionMode != null) val = resolutionMode.getMask();
    if (sensorReading != null) val = val | sensorReading.getMask();
    return displayRegister(length, getAddress(), val);
  }
}
