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

package io.mapsmessaging.devices.i2c.devices.sensors.bh1750;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.PowerManagement;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

public class Bh1750Sensor extends I2CDevice implements PowerManagement, Sensor {

  private static final byte POWER_DOWN = 0b00000000;
  private static final byte POWER_UP = 0b00000001;
  private static final byte RESET = 0b00000111;

  @Getter
  private ResolutionMode resolutionMode;

  @Getter
  private SensorReading sensorReading;

  private int lux;
  private long lastRead;

  public Bh1750Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(Bh1750Sensor.class));
    lastRead = 0;
    resolutionMode = ResolutionMode.H_RESOLUTION_MODE;
    sensorReading = SensorReading.CONTINUOUS;
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      initialise();
    }
  }

  public void setResolutionMode(ResolutionMode mode) throws IOException {
    resolutionMode = mode;
    write(resolutionMode.getMask() | sensorReading.getMask());
  }


  public void setSensorReading(SensorReading sensor) throws IOException {
    sensorReading = sensor;
    write(resolutionMode.getMask() | sensorReading.getMask());
  }


  @Override
  public boolean isConnected() {
    return true;
  }

  public void powerOn() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "powerOn()");
    }
    write(POWER_UP);

    delay(10);
  }

  public void reset() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "reset()");
    }
    write(RESET);
    delay(10);
  }

  public void powerOff() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "powerOff()");
    }
    write(POWER_DOWN);
  }

  public void initialise() throws IOException {
    powerOn();
    write(resolutionMode.getMask() | sensorReading.getMask());
  }

  private void scanForChange() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      lastRead = System.currentTimeMillis() + 120;
      byte[] data = new byte[2];
      read(data);
      lux = (data[0] & 0xff) << 8 | (data[1] & 0xff);
    }
  }

  public float getLux() throws IOException {
    scanForChange();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), lux + " = getCurrentValue()");
    }
    return (lux / 1.2f) / resolutionMode.getAdjustment();
  }


  @Override
  public String getName() {
    return "BH1750";
  }

  @Override
  public String getDescription() {
    return "Light sensor and Lux computation";
  }
}