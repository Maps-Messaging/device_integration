/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StartLowPowerPeriodicMeasurementRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StartPeriodicMeasurementRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StopPeriodicMeasurementRequest;

public class PeriodicMeasurementRegister extends RequestRegister {

  private final StartPeriodicMeasurementRequest startRequest;
  private final StopPeriodicMeasurementRequest stopRequest;
  private final StartLowPowerPeriodicMeasurementRequest lowPowerRequest;

  public PeriodicMeasurementRegister(I2CDevice sensor) {
    super(sensor, "PeriodicMeasurement", null);
    this.startRequest = new StartPeriodicMeasurementRequest(sensor.getDevice());
    this.stopRequest = new StopPeriodicMeasurementRequest(sensor.getDevice());
    lowPowerRequest = new StartLowPowerPeriodicMeasurementRequest(sensor.getDevice());
  }

  public void startPeriodicMeasurement() {
    startRequest.getResponse();
  }

  public void startLowPowerMeasurement() {
    lowPowerRequest.getResponse();
  }

  public void stopPeriodicMeasurement() {
    stopRequest.getResponse();
  }
}
