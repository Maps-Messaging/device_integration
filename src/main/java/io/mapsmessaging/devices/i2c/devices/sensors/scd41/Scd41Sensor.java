package io.mapsmessaging.devices.i2c.devices.sensors.scd41;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class Scd41Sensor extends I2CDevice implements Sensor, Resetable, PowerManagement {


  private final DataReadyRegister dataReadyRegister;
  private final FactoryResetRegister factoryResetRegister;
  private final GetAltitudeRegister getAltitudeRegister;
  private final GetSerialNumberRegister getSerialNumberRegister;
  private final ReadMeasurementRegister readMeasurementRegister;
  private final StartPeriodicMeasurementRegister startPeriodicMeasurementRegister;
  private final StopPeriodicMeasurementRegister stopPeriodicMeasurementRegister;
  private final PowerDownRegister powerDownRegister;
  private final WakeUpRegister wakeUpRegister;
  private final List<SensorReading<?>> readings;

  public Scd41Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Scd41Sensor.class));
    dataReadyRegister = new DataReadyRegister(this);
    factoryResetRegister = new FactoryResetRegister(this);
    getAltitudeRegister = new GetAltitudeRegister(this);
    getSerialNumberRegister = new GetSerialNumberRegister(this);
    readMeasurementRegister = new ReadMeasurementRegister(this);
    startPeriodicMeasurementRegister = new StartPeriodicMeasurementRegister(this);
    stopPeriodicMeasurementRegister = new StopPeriodicMeasurementRegister(this);
    powerDownRegister = new PowerDownRegister(this);
    wakeUpRegister = new WakeUpRegister(this);
    IntegerSensorReading co2Sensor = new IntegerSensorReading("CO2", "ppm", 0, 40_000, readMeasurementRegister::getCo2);
    FloatSensorReading humidity = new FloatSensorReading("Humidity", "%RH", 0, 100.0f, 2, readMeasurementRegister::getHumidity);
    FloatSensorReading temperature = new FloatSensorReading("Temperature", "°C", -10, 60.0f, 2, readMeasurementRegister::getTemperature);
    readings = List.of(co2Sensor, humidity, temperature);
    initialise();
  }

  private void initialise() throws IOException {
    startPeriodicMeasurementRegister.startPeriodicMeasurement();
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
    return "CO2 Sensor 400 to 2000 ppm";
  }

  @Override
  public void reset() throws IOException {
    factoryResetRegister.factoryResetDevice();
  }

  @Override
  public void softReset() throws IOException {
    stopPeriodicMeasurementRegister.stopPeriodicMeasurement();
    initialise();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public void powerOn() throws IOException {
    wakeUpRegister.wakeUp();
  }

  @Override
  public void powerOff() {
    powerDownRegister.powerDown();
  }
}
