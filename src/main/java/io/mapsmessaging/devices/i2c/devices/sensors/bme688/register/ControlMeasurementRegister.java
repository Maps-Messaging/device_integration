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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ControlMeasurement;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.Oversampling;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.PowerMode;

import java.io.IOException;

public class ControlMeasurementRegister extends SingleByteRegister {

  public ControlMeasurementRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x74, "Ctrl_meas");
    reload();
  }

  public PowerMode getMode() {
    return PowerMode.values()[registerValue & 0b11];
  }

  public void setPowerMode(PowerMode mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b11111100) | (val & 0b11));
  }

  public Oversampling getTemperatureOverSampling() {
    int idx = (0xff & registerValue) >> 5;
    if (idx > Oversampling.values().length) {
      return Oversampling.values()[0];
    }
    return Oversampling.values()[idx];
  }

  public void setTemperatureOversampling(Oversampling mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b00011111) | (val & 0b111) << 5);
  }

  public Oversampling getPressureOverSampling() {
    int idx = (registerValue >> 2) & 0b111;
    return Oversampling.values()[idx];
  }

  public void setPressureOversampling(Oversampling mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b11100011) | (val & 0b111) << 2);
  }

  public void updateRegister() throws IOException {
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ControlMeasurement(getMode(), getTemperatureOverSampling(), getPressureOverSampling());
  }

}
