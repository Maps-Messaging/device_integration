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

package io.mapsmessaging.devices.i2c.devices.sensors.am2320;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_DEVICE_READ_REQUEST;

public class AM2320Sensor extends I2CDevice implements Sensor {

  private static final int AM2320_SENSOR_VERSION = 1;
  /// < the sensor version
  private static final int AM2320_CMD_READREG = 0x03;
  /// < read register command
  private static final int AM2320_REG_TEMP_H = 0x02;
  /// < temp register address
  private static final int AM2320_REG_HUM_H = 0x00;
  /// < humidity register address
  @Getter
  private final List<SensorReading<?>> readings;
  private float temperature;
  private float humidity;
  private long lastRead;

  public AM2320Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(AM2320Sensor.class));
    lastRead = 0;

    FloatSensorReading temperatureReading = new FloatSensorReading(
        "temperature",
        "°C",
        "Ambient temperature from AM2320 sensor",
        22.5f,
        true,
        -40f,
        80f,
        1,
        this::getTemperature
    );

    FloatSensorReading humidityReading = new FloatSensorReading(
        "humidity",
        "%",
        "Relative humidity from AM2320 sensor",
        55.0f,
        true,
        0f,
        100f,
        0,
        this::getHumidity
    );

    readings = List.of(temperatureReading, humidityReading);
    loadValues();
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  public float getTemperature() throws IOException {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getTemperature()", temperature);
    }
    return temperature;
  }

  public float getHumidity() throws IOException {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getHumidity()", humidity);
    }
    return humidity;
  }

  public void loadValues() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      byte[] response = new byte[8];
      try {
        write(0);
      } catch (Exception e) {
        // Wake it up.. it sleeps all the time
      }
      delay(10);
      response[0] = (byte) AM2320_CMD_READREG;
      response[1] = (byte) AM2320_REG_HUM_H;
      response[2] = (byte) 4;
      write(response, 0, 3);
      delay(2);
      read(response);
      int crc = (((response[7] & 0xff) << 8) | (response[6] & 0xff));
      int computedCrc = crc16(response, 6);

      if (response[0] != 0x3 && response[1] != 4 && crc != computedCrc) {
        humidity = -273.0f;
        temperature = -273.0f;
        return;
      }
      humidity = (((response[2] & 0xff) << 8) | (response[3] & 0xff)) / 10.0f;
      temperature = (((response[4] & 0xff) << 8) | (response[5] & 0xff)) / 10.0f;
      lastRead = System.currentTimeMillis() + 1000;
    }
  }

  public int crc16(byte[] data, int len) {
    int crc = 0xFFFF;
    for (int j = 0; j < len; j++) {
      crc ^= (data[j] & 0xFF); // Make sure to do bitwise AND with 0xFF, because Java doesn't have unsigned bytes
      for (int i = 0; i < 8; i++) {
        if ((crc & 0x01) != 0) {
          crc >>= 1;
          crc ^= 0xA001;
        } else {
          crc >>= 1;
        }
      }
    }
    return crc & 0xFFFF; // Return only the lower 16 bits
  }


  @Override
  public String getName() {
    return "AM2320";
  }

  @Override
  public String getDescription() {
    return "Temperature and Humidity sensor";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}