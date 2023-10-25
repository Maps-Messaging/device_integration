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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class GasSensor extends I2CDevice implements Sensor {

  private final SensorType sensorType;

  @Getter
  private final I2CRegister i2CRegister;

  @Getter
  private final AcquireModeRegister acquireModeRegister;

  @Getter
  private final ThresholdAlarmRegister thresholdAlarmRegister;

  @Getter
  private final ConcentrationRegister concentrationRegister;

  @Getter
  private final TemperatureRegister temperatureRegister;

  @Getter
  private final SensorReadingRegister sensorReadingRegister;

  @Getter
  private final VoltageRegister voltageRegister;

  @Getter
  private final List<SensorReading<?>> readings;

  public GasSensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(GasSensor.class));
    concentrationRegister = new ConcentrationRegister(this);
    sensorType = concentrationRegister.getSensorType();
    i2CRegister = new I2CRegister(this);
    acquireModeRegister = new AcquireModeRegister(this);
    thresholdAlarmRegister = new ThresholdAlarmRegister(this);
    temperatureRegister = new TemperatureRegister(this);
    sensorReadingRegister = new SensorReadingRegister(this);
    voltageRegister = new VoltageRegister(this);
    FloatSensorReading concentration = new FloatSensorReading("concentration", sensorType.getUnits(), sensorType.getMinimumRange(), sensorType.getMaximumRange(), sensorType.getResolution(), this::getConcentration);
    FloatSensorReading concentrationTempAdj = new FloatSensorReading("concentrationTempAdj", sensorType.getUnits(), sensorType.getMinimumRange(), sensorType.getMaximumRange(), sensorType.getResolution(), this::getConcentration);
    FloatSensorReading temperature = new FloatSensorReading("temperature", "C", -30, 70, 1, this::getTemperature);
    readings = List.of(temperature, concentration, concentrationTempAdj);
  }

  protected float getTemperatureAdjustedConcentration() throws IOException {
    float concentration = concentrationRegister.getConcentration();
    if (sensorType != null && concentration > 0) {
      return sensorType.getSensorModule().
          computeGasConcentration(temperatureRegister.getTemperature(), concentration);
    }
    return 0;
  }


  protected float getTemperature() throws IOException {
    return sensorReadingRegister.getTemperature();
  }

  protected float getConcentration() throws IOException {
    return sensorReadingRegister.getConcentration();
  }


  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    if (sensorType != null) {
      return sensorType.getSku();
    }
    return "GasSensor";
  }

  @Override
  public String getDescription() {
    if (sensorType != null) {
      return sensorType.name() + " gas sensor detects from " + sensorType.getMinimumRange() +
          " to " + sensorType.getMaximumRange() + " " + sensorType.getUnits();
    }
    return "Generic Gas Sensor";
  }
  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}