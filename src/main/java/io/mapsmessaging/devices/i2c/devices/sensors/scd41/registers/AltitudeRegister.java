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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetAltitudeRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetAltitudeRequest;

public class AltitudeRegister extends RequestRegister {
  private final GetAltitudeRequest getAltitudeRequest;
  private final SetAltitudeRequest setAltitudeRequest;

  public AltitudeRegister(I2CDevice sensor) {
    // Initialize both request types upon creation
    super(sensor, "Altitude", null); // Initially, there's no default request to associate
    this.getAltitudeRequest = new GetAltitudeRequest(sensor.getDevice());
    this.setAltitudeRequest = new SetAltitudeRequest(sensor.getDevice());
  }

  public int getAltitude() {
    // Use the getAltitudeRequest to read the altitude
    return getAltitudeRequest.getAltitude();
  }

  public void setAltitude(int val) {
    // Use the setAltitudeRequest to update the altitude
    setAltitudeRequest.setAlititude(val); // Note: There's a typo in "setAlititude", should be "setAltitude"
  }
}