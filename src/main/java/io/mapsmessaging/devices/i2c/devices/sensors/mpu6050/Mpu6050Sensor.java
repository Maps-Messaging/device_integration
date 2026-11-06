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

package io.mapsmessaging.devices.i2c.devices.sensors.mpu6050;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import java.io.IOException;
import java.util.List;

import static io.mapsmessaging.devices.util.Constants.EARTH_GRAVITY_FLOAT;

public class Mpu6050Sensor extends I2CDevice implements Sensor {
  static final int WHO_AM_I = 0x75;
  static final int CHIP_ID = 0x68;
  private static final float ACCELERATION_SCALE = EARTH_GRAVITY_FLOAT / 16384f;
  private static final float GYRO_SCALE = (float) (Math.PI / (180.0 * 131.0));
  private static final float ACCELERATION_RANGE = 2f * EARTH_GRAVITY_FLOAT;
  private static final float GYRO_RANGE = (float) Math.toRadians(250);
  private final List<SensorReading<?>> readings;
  private final ThreadLocal<SampleState> sampleState = new ThreadLocal<>();

  private record Sample(float ax, float ay, float az, float temperature, float gx, float gy, float gz) { }

  private static final class SampleState {
    private Sample sample;
    private IOException failure;
  }

  public Mpu6050Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Mpu6050Sensor.class));
    if ((device.getDevice() != 0x68 && device.getDevice() != 0x69) || readRegister(WHO_AM_I) != CHIP_ID) {
      throw new IOException("Unexpected MPU6050 identity or address");
    }
    // Wake all axes, use X gyro PLL, and disable FIFO, auxiliary master and interrupts.
    write(0x6b, (byte) 0x01);
    write(0x6c, (byte) 0x00);
    write(0x6a, (byte) 0x00);
    write(0x23, (byte) 0x00);
    write(0x38, (byte) 0x00);
    // DLPF setting 3 gives a 1 kHz base; divider 9 selects 100 Hz.
    write(0x1a, (byte) 0x03);
    write(0x19, (byte) 0x09);
    write(0x1b, (byte) 0x00); // +/-250 degrees/s, 131 LSB per degree/s.
    write(0x1c, (byte) 0x00); // +/-2 g, 16384 LSB/g.
    delay(100);
    readings = generateSensorReadings(List.of(
        acceleration("x_acceleration", "X-axis acceleration", this::getXAcceleration),
        acceleration("y_acceleration", "Y-axis acceleration", this::getYAcceleration),
        acceleration("z_acceleration", "Z-axis acceleration", this::getZAcceleration),
        angularVelocity("x_angular_velocity", "X-axis angular velocity", this::getXAngularVelocity),
        angularVelocity("y_angular_velocity", "Y-axis angular velocity", this::getYAngularVelocity),
        angularVelocity("z_angular_velocity", "Z-axis angular velocity", this::getZAngularVelocity),
        new FloatSensorReading("temperature", "°C", "Internal die temperature", 36.53f, true, -40f, 85f, 2, this::getTemperature)));
  }

  private static FloatSensorReading acceleration(String name, String description, ReadingSupplier<Float> supplier) {
    return new FloatSensorReading(name, "m/s²", description, 0f, true, -ACCELERATION_RANGE, ACCELERATION_RANGE, 5, supplier);
  }

  private static FloatSensorReading angularVelocity(String name, String description, ReadingSupplier<Float> supplier) {
    return new FloatSensorReading(name, "rad/s", description, 0f, true, -GYRO_RANGE, GYRO_RANGE, 6, supplier);
  }

  void beginSample() { sampleState.set(new SampleState()); }
  void endSample() { sampleState.remove(); }

  private Sample getSample() throws IOException {
    SampleState state = sampleState.get();
    if (state == null) return readSample();
    if (state.failure != null) throw state.failure;
    if (state.sample == null) {
      try {
        state.sample = readSample();
      } catch (IOException exception) {
        state.failure = exception;
        throw exception;
      }
    }
    return state.sample;
  }

  private Sample readSample() throws IOException {
    byte[] data = new byte[14];
    if (readRegister(0x3b, data, 0, data.length) != data.length) {
      throw new IOException("Incomplete MPU6050 measurement response");
    }
    return new Sample(word(data, 0) * ACCELERATION_SCALE, word(data, 2) * ACCELERATION_SCALE,
        word(data, 4) * ACCELERATION_SCALE, word(data, 6) / 340f + 36.53f,
        word(data, 8) * GYRO_SCALE, word(data, 10) * GYRO_SCALE, word(data, 12) * GYRO_SCALE);
  }

  private static short word(byte[] data, int offset) {
    return (short) (((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff));
  }

  public float getXAcceleration() throws IOException { return getSample().ax(); }
  public float getYAcceleration() throws IOException { return getSample().ay(); }
  public float getZAcceleration() throws IOException { return getSample().az(); }
  public float getXAngularVelocity() throws IOException { return getSample().gx(); }
  public float getYAngularVelocity() throws IOException { return getSample().gy(); }
  public float getZAngularVelocity() throws IOException { return getSample().gz(); }
  public float getTemperature() throws IOException { return getSample().temperature(); }

  @Override
  public boolean isConnected() {
    try {
      return readRegister(WHO_AM_I) == CHIP_ID;
    } catch (IOException _) {
      return false;
    }
  }

  @Override public String getName() { return "MPU6050"; }
  @Override public String getDescription() { return "MPU-6050 six-axis accelerometer and gyroscope"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }
  @Override public List<SensorReading<?>> getReadings() { return readings; }
}
