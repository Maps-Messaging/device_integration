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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen68MeasurementManager;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen6xMeasurementManager;
import io.mapsmessaging.devices.impl.AddressableDevice;

public class Sen68Sensor extends Sen6xSensor {
  public Sen68Sensor(AddressableDevice device) {
    super(device);
  }


  @Override
  protected Sen6xMeasurementManager contructMeasurementManager(Sen6xCommandHelper helper) {
    return new Sen68MeasurementManager(helper);
  }

}
