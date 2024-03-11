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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class BME688Sensor extends I2CDevice implements PowerManagement, Sensor {


  public BME688Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(BME688Sensor.class));
    initialise();
  }

  @Override
  public String getName() {
    return "BME688";
  }

  @Override
  public String getDescription() {
    return "VOC, Humidity, Temperature and Pressure sensor";
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void powerOn() throws IOException {

  }

  @Override
  public void powerOff() throws IOException {

  }

  private void initialise() throws IOException {

  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}