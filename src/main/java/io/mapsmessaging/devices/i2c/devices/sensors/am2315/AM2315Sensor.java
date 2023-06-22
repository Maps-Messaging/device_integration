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
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

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
  private static final byte LOW_RH = 0x01;
  private static final byte HIGH_TEMP = 0x02;
  private static final byte LOW_TEMP = 0x03;
  private static final byte Retention1 = 0x04;
  private static final byte Retention2 = 0x05;
  private static final byte Retention3 = 0x06;
  private static final byte Retention4 = 0x07;
  private static final byte MODEL_HIGH = 0x08;
  private static final byte MODEL_LOW = 0x09;
  private static final byte VERSION = 0x0A;
  private static final byte ID_24_31 = 0x0B;
  private static final byte ID_16_23 = 0x0C;
  private static final byte ID_8_15 = 0x0D;
  private static final byte ID_0_7 = 0x0E;
  private static final byte STATUS = 0x0F;
  private static final byte USER_REGISTER1_HIGH = 0x10;
  private static final byte USER_REGISTER1_LOW = 0x11;
  private static final byte USER_REGISTER2_HIGH = 0x12;
  private static final byte USER_REGISTER2_LOW = 0x13;
  private static final byte Retention5 = 0x14;
  private static final byte Retention6 = 0x15;
  private static final byte Retention7 = 0x16;
  private static final byte Retention8 = 0x17;
  private static final byte Retention9 = 0x18;
  private static final byte RetentionA = 0x19;
  private static final byte RetentionB = 0x1A;
  private static final byte RetentionC = 0x1B;
  private static final byte RetentionD = 0x1C;
  private static final byte RetentionE = 0x1D;
  private static final byte RetentionF = 0x1E;
  private static final byte Retention10 = 0x1F;

  private final Logger logger = LoggerFactory.getLogger("AM2315");
  private byte[] sensorReadings = new byte[4];
  private long lastRead;


  public AM2315Sensor(I2C device) throws IOException {
    super(device);
    lastRead = 0;
    loadValues();
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  private void loadValues() throws IOException {
    sensorReadings = retryReads(HIGH_RH, Retention1);
  }

  public float getTemperature() {
    int val = (sensorReadings[2] & 0xff) << 8 | (sensorReadings[3] & 0xff) + 10;
    return val / 10.0f;
  }

  public float getHumidity() {
    int val = (sensorReadings[0] & 0xff) << 8 | (sensorReadings[1] & 0xff);
    return val / 10.0f;
  }

  public long getId() {
    try {
      byte[] ret = retryReads(ID_24_31, (byte) 0x4);
      long res = 0;
      for (int x = 0; x < 4; x++) {
        res = res << 8 | (ret[x] & 0xff);
      }
      return res;
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getModel() {
    try {
      byte[] ret = retryReads(MODEL_HIGH, (byte) 0x2);
      return (ret[0] & 0xff) << 8 | (ret[1] & 0xff);
    } catch (IOException e) {
      //  logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getVersion() {
    try {
      byte[] ret = retryReads(VERSION, (byte) 0x1);
      return (ret[0] & 0xff);
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public int getStatus() {
    try {
      byte[] ret = retryReads(STATUS, (byte) 0x1);
      return (ret[0] & 0xff);
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
    return 0;
  }

  public void scanForChange() {
    try {
      if (lastRead + 1000 < System.currentTimeMillis()) {
        lastRead = System.currentTimeMillis();
        byte[] val = retryReads(HIGH_RH, Retention1);
        boolean changed = false;
        for (int x = 0; x < val.length; x++) {
          if (val[x] != sensorReadings[x]) {
            changed = true;
            sensorReadings = val;
            break;
          }
        }
      }
    } catch (IOException e) {
      //logger.debug(e.getMessage());
    }
  }


  private byte[] retryReads(byte start, byte end) throws IOException {
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

  @Override
  public String getName() {
    return "AM2315";
  }

  @Override
  public String getDescription() {
    return "Encased Temperature and Humidity sensor";
  }
}