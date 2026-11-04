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

package io.mapsmessaging.devices.i2c.devices.sensors.bmp280;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import java.io.IOException;
import java.util.List;
import lombok.Getter;

public class BMP280Sensor extends I2CDevice implements Sensor {

  // Legacy public constants retained for source compatibility; BMP280 uses the register protocol below.
  public static final byte PROM_READ_SEQUENCE = (byte) 0xA0;
  public static final byte ADC_READ = (byte) 0x00;

  private final int[] calibration = new int[12];
  @Getter
  private final List<SensorReading<?>> readings;
  private long lastRead;
  private float temperature;
  private float pressure;

  public BMP280Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(BMP280Sensor.class));
    if (readRegister(0xd0) != 0x58) {
      throw new IOException("Unexpected BMP280 chip identity");
    }
    write(0xe0, (byte) 0xb6);
    delay(5);
    waitUntilReady();
    byte[] coefficients = new byte[24];
    read((byte) 0x88, coefficients.length, coefficients);
    for (int index = 0; index < calibration.length; index++) {
      int value = (coefficients[index * 2] & 255) | ((coefficients[index * 2 + 1] & 255) << 8);
      calibration[index] = index == 0 || index == 3 ? value : (short) value;
    }
    if (calibration[0] == 0 || calibration[0] == 0xffff || calibration[3] == 0 || calibration[3] == 0xffff) {
      throw new IOException("Invalid BMP280 calibration coefficients");
    }
    FloatSensorReading temperatureReading = new FloatSensorReading(
        "temperature", "°C", "Temperature reading from BMP280 sensor", 25.0f, true, -40f, 85f, 1, this::getTemperature);
    FloatSensorReading pressureReading = new FloatSensorReading(
        "pressure", "hPa", "Pressure reading from BMP280 sensor", 1013.25f, true, 300f, 1100f, 1, this::getPressure);
    readings = generateSensorReadings(List.of(temperatureReading, pressureReading));
  }

  @Override
  public String getName() {
    return "BMP280";
  }

  @Override
  public String getDescription() {
    return "Temperature and Pressure sensor";
  }

  @Override
  public boolean isConnected() {
    try {
      return readRegister(0xd0) == 0x58;
    } catch (IOException exception) {
      return false;
    }
  }

  public synchronized float getTemperature() throws IOException {
    loadValues();
    return temperature;
  }

  public synchronized float getPressure() throws IOException {
    loadValues();
    return pressure;
  }

  protected void read(byte command, int length, byte[] values) throws IOException {
    if (readRegister(command & 255, values, 0, length) != length) {
      throw new IOException("Incomplete BMP280 register response");
    }
  }

  private void waitUntilReady() throws IOException {
    for (int attempt = 0; attempt < 10; attempt++) {
      if ((readRegister(0xf3) & 9) == 0) {
        return;
      }
      delay(2);
    }
    throw new IOException("BMP280 conversion or calibration copy is not ready");
  }

  private void loadValues() throws IOException {
    if (System.currentTimeMillis() < lastRead) {
      return;
    }
    // Forced mode, temperature and pressure oversampling x1 (maximum conversion time 6.425 ms).
    write(0xf4, (byte) 0x25);
    delay(10);
    waitUntilReady();
    byte[] data = new byte[6];
    read((byte) 0xf7, data.length, data);
    int rawPressure = ((data[0] & 255) << 12) | ((data[1] & 255) << 4) | ((data[2] & 255) >>> 4);
    int rawTemperature = ((data[3] & 255) << 12) | ((data[4] & 255) << 4) | ((data[5] & 255) >>> 4);
    if (rawPressure == 0x80000 || rawTemperature == 0x80000) {
      throw new IOException("BMP280 measurement is unavailable");
    }
    // Bosch BMP280 datasheet section 8.1 floating point compensation, pressure in Pa.
    double temperatureDelta = rawTemperature / 131072.0 - calibration[0] / 8192.0;
    double fineTemperature = (rawTemperature / 16384.0 - calibration[0] / 1024.0) * calibration[1]
        + temperatureDelta * temperatureDelta * calibration[2];
    double first = ((int) fineTemperature) / 2.0 - 64000.0;
    double second = first * first * calibration[8] / 32768.0 + first * calibration[7] * 2.0;
    second = second / 4.0 + calibration[6] * 65536.0;
    first = (calibration[5] * first * first / 524288.0 + calibration[4] * first) / 524288.0;
    first = (1.0 + first / 32768.0) * calibration[3];
    if (first == 0.0) {
      throw new IOException("BMP280 pressure calibration divisor is zero");
    }
    double pressurePa = (1048576.0 - rawPressure - second / 4096.0) * 6250.0 / first;
    pressurePa += (calibration[11] * pressurePa * pressurePa / 2147483648.0
        + pressurePa * calibration[10] / 32768.0 + calibration[9]) / 16.0;
    temperature = (float) (fineTemperature / 5120.0);
    pressure = (float) (pressurePa / 100.0);
    lastRead = System.currentTimeMillis() + 100;
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }
}
