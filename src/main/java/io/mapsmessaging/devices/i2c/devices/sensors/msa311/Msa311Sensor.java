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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.PowerManagement;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class Msa311Sensor extends I2CDevice implements Sensor, PowerManagement {

  public Msa311Sensor(I2C device) {
    super(device, LoggerFactory.getLogger(Msa311Sensor.class));
  }

  @Override
  public boolean isConnected() {
    return true;
  }


  @Override
  public String getName() {
    return "MSA311";
  }

  @Override
  public String getDescription() {
    return "Digital Tri-axial Accelerometer";
  }


  @Override
  public void powerOn() throws IOException {

  }

  @Override
  public void powerOff() throws IOException {

  }
}