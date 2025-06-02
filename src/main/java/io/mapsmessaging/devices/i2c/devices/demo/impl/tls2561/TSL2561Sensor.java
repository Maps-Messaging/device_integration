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

package io.mapsmessaging.devices.i2c.devices.demo.impl.tls2561;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.demo.SimulatedIntValue;
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

  @Getter
  private final List<SensorReading<?>> readings;
  private final SimulatedIntValue fullLightSim;
  private final SimulatedIntValue irLightSim;


  public TSL2561Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(TSL2561Sensor.class));
    fullLightSim = new SimulatedIntValue(100, 40000, 32000, 500);
    irLightSim = new SimulatedIntValue(50, 20000, 15000, 300);


    IntegerSensorReading full = new IntegerSensorReading(
        "full",
        "",
        "Raw full-spectrum light reading from TSL2561",
        32000,
        true,
        0,
        0xFFFF,
        this::getFull
    );

    IntegerSensorReading ir = new IntegerSensorReading(
        "ir",
        "",
        "Raw infrared light reading from TSL2561",
        15000,
        true,
        0,
        0xFFFF,
        this::getIr
    );

    FloatSensorReading lux = new FloatSensorReading(
        "lux",
        "lx",
        "Calculated illuminance (lux) from TSL2561 sensor",
        550.0f,
        true,
        0f,
        65535f,
        1,
        this::calculateLux
    );

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

  }

  public void powerOff() throws IOException {
  }

  public void initialise() throws IOException {
  }

  protected int getFull() {
    return fullLightSim.next();
  }

  protected int getIr() {
    return irLightSim.next();
  }


  protected float calculateLux() throws IOException {
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
    };
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