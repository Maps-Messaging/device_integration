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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class Sht31Sensor extends I2CDevice implements PowerManagement, Resetable, Sensor {

  @Getter
  private final List<SensorReading<?>> readings;
  private final ReadDataCommand readDataCommand;
  private final PeriodicReadCommand periodicReadCommand;
  private final SoftResetCommand softResetCommand = new SoftResetCommand();

  public Sht31Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Sht31Sensor.class));
    periodicReadCommand = new PeriodicReadCommand(Repeatability.MEDIUM, Mps.MPS_1);
    readDataCommand = periodicReadCommand.getReadCommand();

    FloatSensorReading temperature = new FloatSensorReading(
        "Temperature",
        "°C",
        "Current Temperature",
        24.2f,
        true,
        0.0f,
        65.0f,
        1,
        this::getTemperature
    );

    FloatSensorReading humidity = new FloatSensorReading(
        "Humidity",
        "%",
        "Current Humidity",
        50.1f,
        true,
        0.0f,
        100.0f,
        1,
        this::getHumidity
    );

    readings = List.of(temperature, humidity);

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
    reset();
    periodicReadCommand.sendCommand(this, device);
  }

  public void powerOff() throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "powerOff()");
    }
    softResetCommand.sendCommand(this, device);
  }

  public void initialise() throws IOException {
    powerOn();
  }

  @Override
  public String getName() {
    return "SHT31DSensor";
  }

  @Override
  public String getDescription() {
    return "Humidity and Temperature Sensor";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public float getTemperature(){
    scanForChange();
    return readDataCommand.getTemperature();
  }

  public float getHumidity(){
    scanForChange();
    return readDataCommand.getHumidity();
  }

  @Override
  public void reset() throws IOException {
    softReset();
  }

  @Override
  public void softReset() throws IOException {
    softResetCommand.sendCommand(this, device);
  }

  private void scanForChange() {
    readDataCommand.read(this, device);
  }

}