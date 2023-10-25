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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class TSL2561Sensor extends I2CDevice implements PowerManagement, Sensor {

  private final ControlRegister controlRegister;
  @Getter
  private final TimingRegister timingRegister;
  @Getter
  private final MultiByteRegister interruptLowThresholdRegister;
  @Getter
  private final MultiByteRegister interruptHighThresholdRegister;
  @Getter
  private final InterruptControlRegister interruptControlRegister;
  @Getter
  private final SingleByteRegister whoAmIRegister;
  @Getter
  private final MultiByteRegister adcData0Register;
  @Getter
  private final MultiByteRegister adcData1Register;

  @Getter
  private final List<SensorReading<?>> readings;

  private long lastRead;

  public TSL2561Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(TSL2561Sensor.class));
    controlRegister = new ControlRegister(this);
    timingRegister = new TimingRegister(this);
    interruptControlRegister = new InterruptControlRegister(this);
    whoAmIRegister = new SingleByteRegister(this, 0x8A, "ID");

    interruptLowThresholdRegister = new LowThresholdRegister(this);
    interruptHighThresholdRegister = new HighThresholdRegister(this);

    adcData0Register = new MultiByteRegister(this, 0x8C, 2, "ADCData0Register");
    adcData1Register = new MultiByteRegister(this, 0x8E, 2, "ADCData1Register");

    IntegerSensorReading full = new IntegerSensorReading("full", "", 0, 0xffff, this::getFull);
    IntegerSensorReading ir = new IntegerSensorReading("in", "", 0, 0xffff, this::getIr);
    FloatSensorReading lux = new FloatSensorReading("lux", "lx", 0, 0xffff, 1, this::calculateLux);
    readings = List.of(full, ir, lux);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      initialise();
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public void powerOn() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "powerOn()");
    }
    controlRegister.powerOn();
  }

  public void powerOff() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "powerOff()");
    }
    controlRegister.powerOff();
  }

  public void initialise() throws IOException {
    powerOn();
    timingRegister.setManual(false);
    timingRegister.setHighGain(false);
    timingRegister.setIntegrationTime(IntegrationTime.MS_402);
  }

  private void scanForChange() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      lastRead = System.currentTimeMillis() + (int) timingRegister.getIntegrationTime().getTime();
      // Read 4 bytes of data
      // ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
      adcData0Register.reload();
      adcData1Register.reload();
    }
  }

  protected int getIr() throws IOException {
    scanForChange();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "getIr()");
    }
    return (int) (adcData1Register.asLong() & 0xFFFF);
  }

  protected int getFull() throws IOException {
    scanForChange();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), " getFull()");
    }
    return (int) (adcData0Register.asLong() & 0xFFFF);
  }

  protected float calculateLux() throws IOException {
    scanForChange();
    int irValue = getIr();
    int fullValue = getFull();
    float channelRatio = irValue / (float) fullValue;
    float lux;
    if (channelRatio <= 0.5) {
      lux = (0.0304f * fullValue) - ((0.062f * fullValue) * (float) Math.pow(channelRatio, 1.4f));
    } else if (channelRatio <= 0.61f) {
      lux = 0.0224f * fullValue - 0.031f * irValue;
    } else if (channelRatio <= 0.80) {
      lux = 0.0128f * fullValue - 0.0153f * irValue;
    } else if (channelRatio <= 1.30f) {
      lux = 0.00146f * fullValue - 0.00112f * irValue;
    } else {
      lux = 0.0f; // high IR, out of range
    }
    lux = lux * timingRegister.getIntegrationTime().getScale();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), lux + " = calculateLux()");
    }
    return lux;
  }

  @Override
  public String getName() {
    return "TSL2561";
  }

  @Override
  public String getDescription() {
    return "Light sensor and Lux computation";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}