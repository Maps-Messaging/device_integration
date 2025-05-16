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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.CalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.HeaterStep;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.Oversampling;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.PowerMode;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class BME688Sensor extends I2CDevice implements PowerManagement, Sensor {

  private final ChipIdRegister chipIdRegister;
  private final ControlMeasurementRegister controlMeasurementRegister;
  private final ResetRegister resetRegister;
  private final VariantIdRegister variantIdRegister;
  private final ConfigRegister configRegister;
  private final ControlHumidityRegister controlHumidityRegister;
  private final ControlGas0Register controlGas0Register;
  private final ControlGas1Register controlGas1Register;
  private final GasWaitRegister gasWaitSharedRegister;
  private final GasWaitRegister[] gasWaitRegisters;
  private final HeaterResistanceRegister heaterResistanceRegister;
  private final HeaterCurrentRegister heaterCurrentRegister;

  private final SensorReadings[] sensorReadings;

  private final CalibrationData calibrationData;

  @Getter
  private final List<SensorReading<?>> readings;
  private long lastRead = 0;
  private final int readingIndex;

  public BME688Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(BME688Sensor.class));

    chipIdRegister = new ChipIdRegister(this);
    controlMeasurementRegister = new ControlMeasurementRegister(this);
    resetRegister = new ResetRegister(this);
    variantIdRegister = new VariantIdRegister(this);
    configRegister = new ConfigRegister(this);
    controlHumidityRegister = new ControlHumidityRegister(this);
    controlGas0Register = new ControlGas0Register(this);
    controlGas1Register = new ControlGas1Register(this);
    gasWaitSharedRegister = new GasWaitRegister(this, 0x6E, "Gas_wait_shared");

    gasWaitRegisters = new GasWaitRegister[10];
    for (int x = 0; x < gasWaitRegisters.length; x++) {
      gasWaitRegisters[x] = new GasWaitRegister(this, 0x64 + x, "Gas_wait_" + x);
    }

    heaterResistanceRegister = new HeaterResistanceRegister(this);
    heaterCurrentRegister = new HeaterCurrentRegister(this);
    calibrationData = new CalibrationData(this);

    sensorReadings = new SensorReadings[3];
    for (int x = 0; x < sensorReadings.length; x++) {
      sensorReadings[x] = new SensorReadings(this, x, calibrationData);
    }
    readingIndex = 0;

    FloatSensorReading temperature = new FloatSensorReading(
        "temperature", "°C", "Temperature from BME688 sensor", 25.0f, true, -40f, 85f, 1, this::getTemperature
    );

    FloatSensorReading humidity = new FloatSensorReading(
        "humidity", "%RH", "Relative humidity from BME688 sensor", 50.0f, true, 10f, 90f, 1, this::getHumidity
    );

    FloatSensorReading pressure = new FloatSensorReading(
        "pressure", "hPa", "Pressure from BME688 sensor", 1013.25f, true, 300f, 1100f, 1, this::getPressure
    );

    FloatSensorReading gas = new FloatSensorReading(
        "gas", "Ω", "Gas resistance from BME688 sensor", 10000.0f, true, 0f, 65535f, 1, this::getGas
    );
    StringSensorReading heaterStatus = new StringSensorReading(
        "heaterStatus",
        "",
        "Current heater status (ON/OFF)",
        "ON",
        true,
        this::getHeaterStatus
    );
    StringSensorReading gasMode = new StringSensorReading(
        "gasMode",
        "",
        "Current gas sensor profile mode",
        "profile_0",
        true,
        this::getGasProfileMode
    );


    readings = List.of(temperature, humidity, pressure, gas, heaterStatus, gasMode);

    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      initialise();
    }
  }

  private String getGasProfileMode() {
    return "profile_" + readingIndex;
  }

  private String getHeaterStatus() {
    try {
      return controlGas1Register.isRunGas() ? "ON" : "OFF";
    } catch (IOException e) {
      return "ERROR";
    }
  }

  @Override
  public String getName() {
    return "BME688";
  }

  @Override
  public String getDescription() {
    return "VOC, Humidity, Temperature and Pressure sensor";
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void powerOn() throws IOException {
    startForceMode();
  }

  @Override
  public void powerOff() throws IOException {
    controlMeasurementRegister.setPowerMode(PowerMode.SLEEP_MODE);
  }

  private void initialise() throws IOException {
    resetRegister.reset();
    delay(5);
    startForceMode();
  }

  public void startForceMode() throws IOException {
    // Set the sampling rates
    controlHumidityRegister.setHumidityOverSampling(Oversampling.X4);
    controlMeasurementRegister.setTemperatureOversampling(Oversampling.X8);
    controlMeasurementRegister.setPressureOversampling(Oversampling.X2);
    controlMeasurementRegister.updateRegister();

    gasWaitRegisters[0].setTimerSteps(52); // 100ms
    gasWaitRegisters[0].setMultiplicationFactor(1);
    gasWaitRegisters[0].updateRegister();
    byte val = (byte) (calibrationData.getGasCalibrationData().calcResHeat(350, 26) & 0xff);
    heaterResistanceRegister.setHeaterResistance(0, val);

    controlGas1Register.setNbConv(HeaterStep.NONE);
    controlGas1Register.setRunGas(true);
    controlMeasurementRegister.setPowerMode(PowerMode.FORCED_MODE);
    controlMeasurementRegister.updateRegister();
    sensorReadings[0].setDataReady(System.currentTimeMillis() + 1000);
  }

  public void startParallelMode() throws IOException {

  }


  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public float getTemperature() throws IOException {
    checkState();
    return (float) (sensorReadings[readingIndex].getTemperature());
  }

  public float getPressure() throws IOException {
    checkState();
    return (float) (sensorReadings[readingIndex].getPressure());
  }

  public float getHumidity() throws IOException {
    checkState();
    return (float) (sensorReadings[readingIndex].getHumidity());
  }

  public float getGas() throws IOException {
    checkState();
    return (float) (sensorReadings[readingIndex].getGas());
  }

  private void checkState() throws IOException {
    if (sensorReadings[readingIndex].getDataReady() < System.currentTimeMillis()) {
      sensorReadings[readingIndex].doMeasurements();
    }
    if (lastRead < System.currentTimeMillis()) {
      lastRead = System.currentTimeMillis() + 1000;
      startForceMode();
    }
  }
}