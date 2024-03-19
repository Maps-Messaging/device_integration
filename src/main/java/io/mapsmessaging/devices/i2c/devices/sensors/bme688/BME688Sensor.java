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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.GasCalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.HumidityCalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.PressureCalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.TemperatureCalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.FilterSize;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.HeaterStep;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.Oversampling;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.PowerMode;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
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

  private final TemperatureCalibrationData temperatureCalibrationData;
  private final PressureCalibrationData pressureCalibrationData;
  private final HumidityCalibrationData humidityCalibrationData;
  private final GasCalibrationData gasCalibrationData;

  @Getter
  private final List<SensorReading<?>> readings;
  private long lastRead = 0;
  private int readingIndex;

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
    temperatureCalibrationData = new TemperatureCalibrationData(this);
    pressureCalibrationData = new PressureCalibrationData(this);
    humidityCalibrationData = new HumidityCalibrationData(this);
    gasCalibrationData = new GasCalibrationData(this);

    sensorReadings = new SensorReadings[3];
    for (int x = 0; x < sensorReadings.length; x++) {
      sensorReadings[x] = new SensorReadings(this, x, temperatureCalibrationData, humidityCalibrationData, pressureCalibrationData, gasCalibrationData);
    }
    readingIndex = 0;

    FloatSensorReading temperature = new FloatSensorReading("temperature", "°C", -40, 85, 1, this::getTemperature);
    FloatSensorReading humidity = new FloatSensorReading("humidity", "%RH", 10, 90, 1, this::getHumidity);
    FloatSensorReading pressure = new FloatSensorReading("pressure", "hPa", 300, 1100, 1, this::getPressure);
    FloatSensorReading gas = new FloatSensorReading("gas", "Ω", 0, 0xffff, 1, this::getGas);
    readings = List.of(temperature, humidity, pressure, gas);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      initialise();
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
    System.err.println("ChipId:"+Integer.toHexString(chipIdRegister.getChipId()));
    System.err.println("Variant:"+Integer.toHexString(variantIdRegister.getVariantId()));

    temperatureCalibrationData.load();
    pressureCalibrationData.load();
    humidityCalibrationData.load();
    gasCalibrationData.load();
  }

  public void startForceMode() throws IOException {
    // Set the sampling rates
    controlHumidityRegister.setHumidityOverSampling(Oversampling.X1);
    controlMeasurementRegister.setTemperatureOversampling(Oversampling.X2);
    controlMeasurementRegister.setPressureOversampling(Oversampling.X16);
    controlMeasurementRegister.updateRegister();
    configRegister.setFilterSize(FilterSize.SIZE_3);
    // Set the heater details
    gasWaitRegisters[0].setTimerSteps(0x1); // 100ms
    gasWaitRegisters[0].setMultiplicationFactor(1);
    gasWaitRegisters[0].updateRegister();

    byte computed = (byte)gasCalibrationData.calcResHeat(300, 26);
    System.err.println("Computed::"+computed);
    heaterResistanceRegister.setHeaterResistance(0, (byte)0x73);
    controlGas1Register.setNbConv(HeaterStep.NONE);
    controlGas1Register.setRunGas(true);
    controlMeasurementRegister.setPowerMode(PowerMode.FORCED_MODE);
    controlMeasurementRegister.updateRegister();
  }

  public void startParallelMode() throws IOException{

  }


  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public float getTemperature() throws IOException {
    if(lastRead < System.currentTimeMillis()){
      lastRead = System.currentTimeMillis() + 1000;
      startForceMode();
    }
    return (float)(sensorReadings[readingIndex].getTemperature());
  }

  public float getPressure() throws IOException {
    if(lastRead < System.currentTimeMillis()){
      lastRead = System.currentTimeMillis() + 1000;
      startForceMode();
    }
    return (float)(sensorReadings[readingIndex].getPressure());
  }

  public float getHumidity() throws IOException {
    if(lastRead < System.currentTimeMillis()){
      lastRead = System.currentTimeMillis() + 1000;
      startForceMode();
    }
    return (float)(sensorReadings[readingIndex].getHumidity());
  }
  public float getGas() throws IOException {
    if(lastRead < System.currentTimeMillis()){
      lastRead = System.currentTimeMillis() + 1000;
      startForceMode();
    }
    return (float)(sensorReadings[readingIndex].getGas());
  }

}