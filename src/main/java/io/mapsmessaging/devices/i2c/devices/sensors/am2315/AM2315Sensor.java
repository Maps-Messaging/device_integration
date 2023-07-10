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

package io.mapsmessaging.devices.i2c.devices.sensors.am2315;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_DEVICE_READ_REQUEST;

public class AM2315Sensor extends I2CDevice {

  //
  // Command Codes
  //
  private static final byte READ_REGISTER = 0x03; // Read data from one or more registers
  private static final byte WRITE_REGISTER = 0x10; // Multiple sets of binary data is written to mutliple registers
  //
  // Registers
  //
  private static final byte HIGH_RH = 0x00;
  private static final byte RETENTION_1 = 0x04;
  private static final byte MODEL_HIGH = 0x08;
  private static final byte VERSION = 0x0A;
  private static final byte ID_24_31 = 0x0B;
  private static final byte STATUS = 0x0F;

  private float temperature;
  private float humidity;
  private long lastRead;


  public AM2315Sensor(I2C device) {
    super(device, LoggerFactory.getLogger(AM2315Sensor.class));
    lastRead = 0;
    loadValues();
  }

  @Override
  public boolean isConnected() {
    return false;
  }


  @Override
  public String getName() {
    return "AM2315";
  }

  @Override
  public String getDescription() {
    return "Encased Temperature and Humidity sensor";
  }

  public long getId() {
    byte[] ret = retryReads(ID_24_31, (byte) 0x4);
    long res = 0;
    for (int x = 0; x < 4; x++) {
      res = res << 8 | (ret[x] & 0xff);
    }
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getId()", res);
    }
    return res;
  }

  public float getTemperature() {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getTemperature()", temperature);
    }
    return temperature;
  }

  public float getHumidity() {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getHumidity()", humidity);
    }
    return humidity;
  }

  public int getModel() {
    byte[] ret = retryReads(MODEL_HIGH, (byte) 0x2);
    int val = (ret[0] & 0xff) << 8 | (ret[1] & 0xff);
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getModel()", val);
    }
    return val;
  }

  public int getVersion() {
    byte[] ret = retryReads(VERSION, (byte) 0x1);
    int val = (ret[0] & 0xff);
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getVersion()", val);
    }
    return val;
  }

  public int getStatus() {
    byte[] ret = retryReads(STATUS, (byte) 0x1);
    int val = (ret[0] & 0xff);
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getStatus()", val);
    }
    return val;
  }

  private void loadValues() {
    if (lastRead < System.currentTimeMillis()) {
      byte[] sensorReadings = retryReads(HIGH_RH, RETENTION_1);
      temperature = ((sensorReadings[2] & 0xff) << 8 | (sensorReadings[3] & 0xff) + 10) / 10.0f;
      humidity = ((sensorReadings[0] & 0xff) << 8 | (sensorReadings[1] & 0xff)) / 10.0f;
      lastRead = System.currentTimeMillis() + 1000;
    }
  }

  private byte[] retryReads(byte start, byte end) {
    int count = 0;
    while (count < 100) {
      try {
        return readRegisters(start, end);
      } catch (Throwable th) {
        count++;
        delay(10);
      }
    }
    return new byte[0];
  }

  private byte[] readRegisters(byte startReg, byte endReg) throws IOException {
    byte[] sendPacket = new byte[3];
    sendPacket[0] = READ_REGISTER;
    sendPacket[1] = startReg;
    sendPacket[2] = endReg;
    write(sendPacket);
    delay(10);
    byte[] header = new byte[32];
    int received = read(header);
    if (header[0] != 3) {
      throw new IOException("Expected read");
    }
    int len = received - 4;
    if (len > 0) {
      byte[] res = new byte[len];
      System.arraycopy(header, 2, res, 0, res.length);
      return res;
    }
    return new byte[0];
  }

}