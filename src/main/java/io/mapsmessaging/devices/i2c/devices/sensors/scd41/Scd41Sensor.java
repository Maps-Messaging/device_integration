/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.scd41;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SerialNumberRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class Scd41Sensor extends I2CDevice implements Sensor, Resetable, PowerManagement {
  private final DataReadyRegister dataReadyRegister;
  private final DeviceStateRegister deviceStateRegister;
  private final ASCERegister asceRegister;
  private final AltitudeRegister altitudeRegister;
  private final AmbientPressureRegister ambientPressureRegister;
  private final GetSerialNumberRegister getSerialNumberRegister;
  private final ReadMeasurementRegister readMeasurementRegister;
  private final PeriodicMeasurementRegister periodicMeasurementRegister;
  private final PowerManagementRegister powerManagementRegister;
  private final TempOffsetRegister tempOffsetRegister;
  private final ForcedRecalRegister forcedRecalRegister;
  private final CalibrationPeriodRegister calibrationPeriodRegister;
  private final SensorConfigurationRegister sensorConfigurationRegister;
  private final List<SensorReading<?>> readings;

  public Scd41Sensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Scd41Sensor.class));

    dataReadyRegister = new DataReadyRegister(this);
    deviceStateRegister = new DeviceStateRegister(this);
    calibrationPeriodRegister = new CalibrationPeriodRegister(this);
    asceRegister = new ASCERegister(this);
    forcedRecalRegister = new ForcedRecalRegister(this);
    altitudeRegister = new AltitudeRegister(this);
    ambientPressureRegister = new AmbientPressureRegister(this);
    getSerialNumberRegister = new GetSerialNumberRegister(this);
    readMeasurementRegister = new ReadMeasurementRegister(this);
    periodicMeasurementRegister = new PeriodicMeasurementRegister(this);
    tempOffsetRegister = new TempOffsetRegister(this);
    sensorConfigurationRegister = new SensorConfigurationRegister(this);
    powerManagementRegister = new PowerManagementRegister(this);

    IntegerSensorReading co2Sensor = new IntegerSensorReading(
        "CO₂",
        "ppm",
        "Carbon dioxide concentration from SCD41 sensor",
        400,
        true,
        0,
        5000,
        readMeasurementRegister::getCo2
    );

    FloatSensorReading humidity = new FloatSensorReading(
        "humidity",
        "%RH",
        "Relative humidity from SCD41 sensor",
        50.0f,
        true,
        0f,
        100.0f,
        2,
        readMeasurementRegister::getHumidity
    );

    FloatSensorReading temperature = new FloatSensorReading(
        "temperature",
        "°C",
        "Ambient temperature from SCD41 sensor",
        25.0f,
        true,
        -10f,
        60.0f,
        2,
        readMeasurementRegister::getTemperature
    );

    StringSensorReading category = new StringSensorReading(
        "airQuality",
        "",
        "Air quality category based on CO₂ levels",
        "Good",
        true,
        this::getAirQuality
    );

    readings = List.of(co2Sensor, humidity, temperature, category);
    initialise();
  }


  public static boolean detect(AddressableDevice device) {
    SerialNumberRequest request = new SerialNumberRequest(device);
    return request.getSerialNumber() != 0;
  }

  private void initialise() {
    periodicMeasurementRegister.startPeriodicMeasurement();
    asceRegister.setASCEState(false); // If used in doors this will stop it from resetting baseline to 400ppm
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    return "SCD-41";
  }

  @Override
  public String getDescription() {
    return "CO2 Sensor 400 to 2000 - 5000 ppm";
  }

  @Override
  public void reset() throws IOException {
    deviceStateRegister.factoryReset();
  }

  @Override
  public void softReset() throws IOException {
    periodicMeasurementRegister.stopPeriodicMeasurement();
    deviceStateRegister.reInitialize();
    initialise();
  }

  public String getAirQuality() {
    int co2 = readMeasurementRegister.getCo2();
    float humidity = readMeasurementRegister.getHumidity();
    float temperature = readMeasurementRegister.getTemperature();

    String airQuality;
    // Adjusting Excellent to reflect current outdoor CO2 levels
    if (co2 <= 450) {
      airQuality = "Excellent";
    } else if (co2 <= 1000) {
      airQuality = "Fresh";
    } else if (co2 <= 2000) {
      airQuality = "Moderate";
    } else if (co2 <= 2500) {
      airQuality = "Stuffy/Unhealthy for Sensitive Groups";
    } else if (co2 <= 5000) {
      airQuality = "Unhealthy";
    } else if (co2 <= 10000) {
      airQuality = "Very Unhealthy";
    } else {
      airQuality = "Hazardous";
    }

    // Adjustments based on temperature and humidity
    if ((humidity > 60 || humidity < 30) || (temperature > 25 || temperature < 19)) {
      switch (airQuality) {
        case "Excellent":
        case "Fresh":
          airQuality = "Moderate";
          break;
        case "Moderate":
          airQuality = "Stuffy/Unhealthy for Sensitive Groups";
          break;
        case "Stuffy/Unhealthy for Sensitive Groups":
          airQuality = "Unhealthy";
          break;
      }
      // No adjustment for higher categories to avoid overestimating the risk
    }

    return airQuality;
  }


  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public void powerOn() throws IOException {
    powerManagementRegister.wakeUp();
  }

  @Override
  public void powerOff() {
    powerManagementRegister.powerDown();
  }
}
