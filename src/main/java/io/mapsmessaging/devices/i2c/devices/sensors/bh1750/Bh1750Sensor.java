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

import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.register.ReadingModeRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.ResolutionMode;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.SensorReadingMode;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class Bh1750Sensor extends I2CDevice implements PowerManagement, Sensor, Resetable {

  private static final byte POWER_DOWN = 0b00000000;
  private static final byte POWER_UP = 0b00000001;
  private static final byte RESET = 0b00000111;

  @Getter
  private final ReadingModeRegister readingModeRegister;

  @Getter
  private final List<SensorReading<?>> readings;

  private int lux;
  private long lastRead;

  public Bh1750Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Bh1750Sensor.class));
    lastRead = 0;
    readingModeRegister = new ReadingModeRegister(this, 0, "Mode");
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      initialise();
    }
    FloatSensorReading luxReading = new FloatSensorReading("lux", "lx", 0, 0xffff, this::getLux);
    readings = List.of(luxReading);
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

  @Override
  public void softReset() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "softReset()");
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
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "initialise()");
    }
    powerOn();
    readingModeRegister.setResolutionMode(ResolutionMode.H_RESOLUTION_MODE);
    readingModeRegister.setSensorReading(SensorReadingMode.CONTINUOUS);
  }

  @Override
  public String getName() {
    return "BH1750";
  }

  @Override
  public String getDescription() {
    return "Light sensor and Lux computation";
  }


  private void scanForChange() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      lastRead = System.currentTimeMillis() + readingModeRegister.getResolutionMode().getDelay();
      byte[] data = new byte[2];
      read(data);
      lux = (data[0] & 0xff) << 8 | (data[1] & 0xff);
    }
  }

  private float getLux() throws IOException {
    scanForChange();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), lux + " = getCurrentValue()");
    }
    return (lux / 1.2f) / readingModeRegister.getResolutionMode().getAdjustment();
  }

}