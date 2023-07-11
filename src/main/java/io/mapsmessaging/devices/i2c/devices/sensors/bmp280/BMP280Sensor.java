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

package io.mapsmessaging.devices.i2c.devices.sensors.bmp280;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_DEVICE_READ_REQUEST;

public class BMP280Sensor extends I2CDevice {

  public static final byte CONVERT_D1_OSR_256 = (byte) 0x40;
  public static final byte CONVERT_D1_OSR_512 = (byte) 0x42;
  public static final byte CONVERT_D1_OSR_1024 = (byte) 0x44;
  public static final byte CONVERT_D1_OSR_2048 = (byte) 0x46;
  public static final byte CONVERT_D1_OSR_4096 = (byte) 0x48;
  public static final byte CONVERT_D2_OSR_256 = (byte) 0x50;
  public static final byte CONVERT_D2_OSR_512 = (byte) 0x52;
  public static final byte CONVERT_D2_OSR_1024 = (byte) 0x54;
  public static final byte CONVERT_D2_OSR_2048 = (byte) 0x56;
  public static final byte CONVERT_D2_OSR_4096 = (byte) 0x58;

  public static final byte PROM_READ_SEQUENCE = (byte) 0xA0;

  public static final byte ADC_READ = (byte) 0x00;
  private static final short sReset = 0x1E;
  private final int[] prom;
  private long C1; // C1
  private long C2; // C2
  private long C3; // C3
  private long C4; // C4
  private long C5; // C5
  private long C6; // C6
  private int CRC;
  private long SENS_T1;
  private long OFF_T1;
  private long TCS;
  private long TCO;
  private int D1; // 24 bit unsigned int
  private int D2; // 24 bit unsigned int

  private float temperature;
  private float pressure;

  private long lastRead;

  public BMP280Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(BMP280Sensor.class));
    prom = new int[8];
    lastRead = 0;
    initialise();
    loadValues();
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
    return true;
  }

  public float getTemperature() {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getTemperature()", temperature);
    }
    return temperature;
  }

  public float getPressure() {
    loadValues();
    if (logger.isDebugEnabled()) {
      logger.log(I2C_BUS_DEVICE_READ_REQUEST, getName(), "getPressure()", pressure);
    }
    return pressure;
  }

  protected void read(byte command, int length, byte[] values) throws IOException {
    readRegister(command, values, 0, length);
  }

  private void conversion() {
    try {
      byte[] readBuffer = new byte[3];
      write(CONVERT_D2_OSR_4096);
      delay(10);
      read(ADC_READ, 3, readBuffer);
      D2 = ((readBuffer[0] & 0xFF) << 16) | ((readBuffer[1] & 0xFF) << 8) | ((readBuffer[2] & 0xFF));
      write(CONVERT_D1_OSR_4096);
      delay(10);
      read(ADC_READ, 3, readBuffer);
      D1 = ((readBuffer[0] & 0xFF) << 16) | ((readBuffer[1] & 0xFF) << 8) | ((readBuffer[2] & 0xFF));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initialise() throws IOException {
    write((byte) sReset);
    delay(1000);
    byte[] readBuffer = new byte[2];
    for (int i = 0; i < 8; i++) {
      read((byte) (PROM_READ_SEQUENCE + i * 2), 2, readBuffer);
      prom[i] = ((readBuffer[0] & 0xFF) << 8) | ((readBuffer[1] & 0xFF));
    }
    C1 = prom[1];
    C2 = prom[2];
    C3 = prom[3];
    C4 = prom[4];
    C5 = prom[5];
    C6 = prom[6];
    CRC = prom[7] & 0x0F;
    byte crc4 = crc4(prom);
    if (crc4 != CRC) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_REQUEST_FAILED, getName(), "initialise()", "crc mismatch " + CRC + " (read) != " + crc4 + " (calculated).");
    }

    SENS_T1 = C1 * (1 << 15) /* 2^15 */;
    OFF_T1 = C2 * (1 << 16) /* 2^16 */;
    TCS = C3 / (1 << 8)  /* 2^8 */;
    TCO = C4 / (1 << 7)  /* 2^7 */;
  }

  private void loadValues() {
    if (lastRead < System.currentTimeMillis()) {
      conversion();
      long dT = D2 - (C5 << 8);
      long t = dT * C6;
      float temp = (t >> 23);
      temp += 2000;
      temperature = temp / 100.0f;

      long off = OFF_T1 + dT * TCO;
      long sens = SENS_T1 + dT * TCS;
      pressure = (((float) (D1 * sens / 2097152.0 /* 2^21 */ - off) / 0x8000 /* 2^15 */) / 100.0f);
      lastRead = System.currentTimeMillis() + 100;
    }
  }

  private byte crc4(int[] prom) {
    int cnt; // simple counter
    int nRem; // crc reminder
    int crcRead; // original value of the crc
    byte nBit;
    nRem = 0x00;
    crcRead = prom[7]; // save read CRC
    prom[7] &= 0xFF00; // CRC byte is replaced by 0
    for (cnt = 0; cnt < 16; cnt++) { // operation is performed on bytes
// choose LSB or MSB
      if (cnt % 2 == 1) {
        nRem ^= prom[cnt >> 1] & 0x00FF;
      } else {
        nRem ^= prom[cnt >> 1] >> 8;
      }
      for (nBit = 8; nBit > 0; nBit--) {
        if ((nRem & 0x8000) == 0x8000) {
          nRem = (nRem << 1) ^ 0x3000;
        } else {
          nRem <<= 1;
        }
      }
    }
    nRem = 0x000F & (nRem >> 12); // final 4-bit reminder is CRC code
    prom[7] = crcRead; // restore the crc_read to its original place
    return (byte) (nRem);
  }
}