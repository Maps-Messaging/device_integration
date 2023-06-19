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

package io.mapsmessaging.devices.i2c.devices.sensors.tls2561;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TLS2561Sensor extends I2CDevice {

  public static int VISIBLE_LIGHT_LEVEL = 0;
  public static int INFERRED_LIGHT_LEVEL = 1;
  public static int LUX_VALUE = 2;

  private final Logger logger = LoggerFactory.getLogger(TLS2561Sensor.class);

  private int full;
  private int ir;
  private byte highGain;
  private long lastRead;

  private IntegrationTime integrationTime;

  public TLS2561Sensor(I2C device) {
    super(device);
    highGain = 0;
    initialise();
    lastRead = 0;
  }

  @Override
  public boolean isConnected() {
    return false;
  }
  public synchronized void setIntegrationTime(IntegrationTime times, boolean highGain) {
    if (highGain) {
      this.highGain = 0x10;
    } else {
      this.highGain = 0x0;
    }
    integrationTime = times;
    write(0x81, (byte) (integrationTime.getSettingValue() | this.highGain));
    delay(500);
  }

  public void powerOn() {
    write(0x80, (byte) 0x03);
    delay(500);
  }

  public void powerOff() {
    write(0x80, (byte) 0x0);
  }

  public synchronized boolean initialise() {
    powerOn();
    setIntegrationTime(IntegrationTime.MS_402, false);
    return true;
  }

  public int[] getLevels() {
    scanForChange();
    int[] result = new int[3];
    result[0] = full;
    result[1] = ir;
    return result;
  }

  private synchronized void scanForChange() {
    if(lastRead< System.currentTimeMillis()) {
      lastRead = System.currentTimeMillis() + integrationTime.getTime();
      // Read 4 bytes of data
      // ch0 lsb, ch0 msb, ch1 lsb, ch1 msb

      byte[] data = new byte[4];
      readRegister(0x8C, data, 0, 4);

      ByteBuffer buffer = ByteBuffer.wrap(data);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      full = buffer.getShort() & 0xFFFF;
      ir = buffer.getShort() & 0xFFFF;
    }
  }

  public double calculateLux() {
    scanForChange();
    double channelRatio = ir / (double) full;

    double lux;
    if (channelRatio <= 0.5) {
      lux = 0.0304 * full - 0.062 * full * Math.pow(channelRatio, 1.4);
    } else if (channelRatio <= 0.61) {
      lux = 0.0224 * full - 0.031 * ir;
    } else if (channelRatio <= 0.80) {
      lux = 0.0128 * full - 0.0153 * ir;
    } else if (channelRatio <= 1.30) {
      lux = 0.00146 * full - 0.00112 * ir;
    } else {
      lux = 0.0; // high IR, out of range
    }
    return lux * integrationTime.getScale();
  }


  public enum IntegrationTime {
    MS_13_7((byte)0, 14, 1.0),
    MS_101((byte)1, 101, 7.8125),
    MS_402((byte)2, 402, 16.0);

    @Getter
    private final int time;
    @Getter
    private final byte settingValue;
    @Getter
    private final double scale;

    IntegrationTime(byte settingValue, int integrationTime, double scale){
      this.settingValue = settingValue;
      this.time = integrationTime;
      this.scale = scale;
    }

  }
}