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

package io.mapsmessaging.devices.i2c.devices.sensors.h3lis200dl;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import java.io.IOException;
import java.util.List;

import static io.mapsmessaging.devices.util.Constants.EARTH_GRAVITY_FLOAT;

public class H3LIS200DLSensor extends I2CDevice implements Sensor {

  static final int WHO_AM_I = 0x0f;
  static final int CHIP_ID = 0x32;
  private static final float RANGE = 100f * EARTH_GRAVITY_FLOAT;
  // ST H3LIS200DL datasheet, table 3: 780 mg/digit at the selected +/-100 g full scale.
  private static final float ACCELERATION_PER_DIGIT = 0.780f * EARTH_GRAVITY_FLOAT;
  private final List<SensorReading<?>> readings;

  public H3LIS200DLSensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(H3LIS200DLSensor.class));
    if (readRegister(WHO_AM_I) != CHIP_ID) {
      throw new IOException("Unexpected H3LIS200DL chip identity");
    }
    // Configure while powered down; bypass the high-pass filter and disable sleep-to-wake.
    write(0x20, (byte) 0x07);
    write(0x21, (byte) 0x00);
    write(0x23, (byte) 0x00);
    write(0x24, (byte) 0x00);
    // Normal mode, 100 Hz, X/Y/Z enabled.
    write(0x20, (byte) 0x2f);
    delay(20);
    FloatSensorReading x = new FloatSensorReading(
        "x_acceleration", "m/s²", "X-axis acceleration", 0f, true, -RANGE, RANGE, 2, this::getX);
    FloatSensorReading y = new FloatSensorReading(
        "y_acceleration", "m/s²", "Y-axis acceleration", 0f, true, -RANGE, RANGE, 2, this::getY);
    FloatSensorReading z = new FloatSensorReading(
        "z_acceleration", "m/s²", "Z-axis acceleration", 0f, true, -RANGE, RANGE, 2, this::getZ);
    readings = generateSensorReadings(List.of(x, y, z));
  }

  public float getX() throws IOException {
    return readAxis(0x29);
  }

  public float getY() throws IOException {
    return readAxis(0x2b);
  }

  public float getZ() throws IOException {
    return readAxis(0x2d);
  }

  private float readAxis(int register) throws IOException {
    byte[] data = new byte[1];
    if (readRegister(register, data, 0, data.length) != data.length) {
      throw new IOException("Incomplete H3LIS200DL axis response");
    }
    // Each axis is an eight-bit two's-complement value; Java byte sign extension is intentional.
    return data[0] * ACCELERATION_PER_DIGIT;
  }

  @Override
  public List<SensorReading<?>> getReadings() {
    return readings;
  }

  @Override
  public boolean isConnected() {
    try {
      return readRegister(WHO_AM_I) == CHIP_ID;
    } catch (IOException _) {
      return false;
    }
  }

  @Override
  public String getName() {
    return "H3LIS200DL";
  }

  @Override
  public String getDescription() {
    return "High-g three-axis accelerometer";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }
}
