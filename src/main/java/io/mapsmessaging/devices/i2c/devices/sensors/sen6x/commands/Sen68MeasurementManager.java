/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data.MeasurementBlock;

import java.io.IOException;

public class Sen68MeasurementManager extends Sen6xMeasurementManager {

  public Sen68MeasurementManager(Sen6xCommandHelper helper) {
    super(helper, 0x0467, 27);
  }

  @Override
  protected MeasurementBlock processResponse(byte[] raw) throws IOException {
    if (raw.length < 18) throw new IOException("Invalid measurement block size");

    return new MeasurementBlock(
        parseUnsignedMeasurement(raw, 0) / 10.0f,       // PM1.0
        parseUnsignedMeasurement(raw, 2) / 10.0f,       // PM2.5
        parseUnsignedMeasurement(raw, 4) / 10.0f,       // PM4.0
        parseUnsignedMeasurement(raw, 6) / 10.0f,       // PM10.0
        parseSignedMeasurement(raw, 8) / 100.0f,      // Humidity
        parseSignedMeasurement(raw, 10) / 200.0f,      // Temperature
        parseSignedMeasurement(raw, 12) / 10.0f,       // VOC Index
        parseSignedMeasurement(raw, 14) / 10.0f,       // NOx Index
        0.0f,                               // CO₂ — not reported by this command
        parseUnsignedMeasurement(raw, 16) / 10.0f       // HCHO ppb
    );
  }
}
