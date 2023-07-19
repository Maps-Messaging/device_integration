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

package io.mapsmessaging.devices.i2c.devices.sensors.bh1750.register;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.data.ReadingModeData;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.ResolutionMode;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.SensorReadingMode;
import lombok.Getter;

import java.io.IOException;

public class ReadingModeRegister extends Register {

  @Getter
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
  public AbstractRegisterData toData() {
    return new ReadingModeData(resolutionMode, sensorReading);
  }

  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
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
  public String toString(int maxLength) {
    return null;
  }
}
