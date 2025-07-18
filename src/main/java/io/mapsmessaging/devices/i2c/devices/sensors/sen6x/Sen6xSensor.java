/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.BooleanSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Getter
public class Sen6xSensor extends I2CDevice implements Sensor, Resetable, PowerManagement {

  private final Sen6xCommandHelper helper;

  private final GetProductNameCommand getProductNameCommand;
  private final GetSerialNumberCommand getSerialNumberCommand;
  private final StartMeasurementCommand startMeasurementCommand;
  private final StopMeasurementCommand stopMeasurementCommand;
  private final SoftResetCommand softResetCommand;
  private final GetDeviceStatusCommand getDeviceStatusCommand;
  private final GetFirmwareVersionCommand getFirmwareVersionCommand;
  private final GetFanCleaningIntervalCommand getFanCleaningIntervalCommand;
  private final SetFanCleaningIntervalCommand setFanCleaningIntervalCommand;
  private final StartFanCleaningCommand startFanCleaningCommand;
  private final ClearDeviceStateCommand clearDeviceStateCommand;
  private final List<SensorReading<?>> readings;
  private final String productName;

  public Sen6xSensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Sen6xSensor.class));
    helper = new Sen6xCommandHelper(device);
    getProductNameCommand = new GetProductNameCommand(helper);
    getSerialNumberCommand = new GetSerialNumberCommand(helper);
    startMeasurementCommand = new StartMeasurementCommand(helper);
    stopMeasurementCommand = new StopMeasurementCommand(helper);
    softResetCommand = new SoftResetCommand(helper);
    getDeviceStatusCommand = new GetDeviceStatusCommand(helper);
    getFirmwareVersionCommand = new GetFirmwareVersionCommand(helper);
    getFanCleaningIntervalCommand = new GetFanCleaningIntervalCommand(helper);
    startFanCleaningCommand = new StartFanCleaningCommand(helper);
    clearDeviceStateCommand = new ClearDeviceStateCommand(helper);
    setFanCleaningIntervalCommand = new SetFanCleaningIntervalCommand(helper);

    productName = getProductNameCommand.get();
    StringSensorReading productNameReading = new StringSensorReading(
        "Product Name",
        "",
        "Product Name",
        "SEN66",
        true,
        getProductNameCommand
    );

    StringSensorReading serialNumberReading = new StringSensorReading(
        "Serial Number",
        "",
        "Product Serial Number",
        "12345678901234567890123456789012",
        true,
        getSerialNumberCommand
    );
    readings = new ArrayList<>();
    readings.add(productNameReading);
    readings.add(serialNumberReading);
    readings.addAll(buildStatusReadings(new Sen6xStatusSupplier(getDeviceStatusCommand)));
    readings.addAll(buildMeasurementReadingds());
    initialise();
  }


  public static boolean detect(AddressableDevice device) {
    try {
      GetSerialNumberCommand request = new GetSerialNumberCommand(new Sen6xCommandHelper(device));
      return !request.get().isEmpty();
    } catch (IOException e) {
      return false;
    }
  }

  private void initialise() {
    try {
      reset();
    } catch (IOException e) {
    }
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
    softResetCommand.reset();
  }

  public void setGetFanCleaningInterval(int interval) {
    setFanCleaningIntervalCommand.set(interval);
  }

  @Override
  public void softReset() throws IOException {
    initialise();
  }


  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public void powerOn() throws IOException {
    startMeasurementCommand.start();
  }

  @Override
  public void powerOff() {
    stopMeasurementCommand.stop();
  }

  public String getFirmwareVersion()  {
    return getFirmwareVersionCommand.get();
  }

  public int getFanCleaningInterval() {
    return getFanCleaningIntervalCommand.get();
  }

  public void startFanCleaning()  {
    startFanCleaningCommand.start();
  }

  public void setFanCleaningInterval(int days) {
    setFanCleaningIntervalCommand.set(days);
  }

  public void clearDeviceState() {
    clearDeviceStateCommand.clear();
  }

  private List<SensorReading<?>> buildMeasurementReadingds() {
    Sen6xMeasurementManager manager = new Sen6xMeasurementManager(helper);
    List<SensorReading<?>> readings = new ArrayList<>();

    EnumSet<Sen6xSensorType> supported = SENSOR_SUPPORT_MAP.getOrDefault(productName.trim().toUpperCase(), EnumSet.of(Sen6xSensorType.CO2));

    for (Sen6xSensorType type : supported) {
      switch (type) {
        case CO2 -> readings.add(new Co2MeasurementCommand(manager).asSensorReading());
        case TEMP -> readings.add(new TemperatureMeasurementCommand(manager).asSensorReading());
        case HUMIDITY -> readings.add(new HumidityMeasurementCommand(manager).asSensorReading());
        case VOC -> readings.add(new VocIndexMeasurementCommand(manager).asSensorReading());
        case NOX -> readings.add(new NoxIndexMeasurementCommand(manager).asSensorReading());
        case PM1 -> readings.add(new Pm1_0MeasurementCommand(manager).asSensorReading());
        case PM2_5 -> readings.add(new Pm2_5MeasurementCommand(manager).asSensorReading());
        case PM4 -> readings.add(new Pm4_0MeasurementCommand(manager).asSensorReading());
        case PM10 -> readings.add(new Pm10_0MeasurementCommand(manager).asSensorReading());
        case HCHO -> readings.add(new HchoMeasurementCommand(manager).asSensorReading());
      }
    }

    AirQualityIndexCommand airQualityIndexCommand = new AirQualityIndexCommand(manager);
    readings.add(new AirQualityLevelCommand(airQualityIndexCommand));
    readings.add(airQualityIndexCommand.asSensorReading());

    return readings;
  }

  private static final Map<String, EnumSet<Sen6xSensorType>> SENSOR_SUPPORT_MAP = Map.of(
      "SEN68", EnumSet.of(Sen6xSensorType.HCHO, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC, Sen6xSensorType.NOX, Sen6xSensorType.PM1, Sen6xSensorType.PM2_5, Sen6xSensorType.PM4, Sen6xSensorType.PM10),
      "SEN66", EnumSet.of(Sen6xSensorType.CO2, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC, Sen6xSensorType.NOX, Sen6xSensorType.PM1, Sen6xSensorType.PM2_5, Sen6xSensorType.PM4, Sen6xSensorType.PM10),
      "SEN65", EnumSet.of(Sen6xSensorType.CO2, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC),
      "SEN64", EnumSet.of(Sen6xSensorType.CO2)
  );

  private List<SensorReading<Boolean>> buildStatusReadings(Sen6xStatusSupplier supplier) {
    return List.of(
        new BooleanSensorReading("Fan Error", "Fan failure detected", "Fan", false, true, supplier::isFanError),
        new BooleanSensorReading("RHT Error", "Humidity/Temperature sensor error", "RHT", false, true, supplier::isRhtError),
        new BooleanSensorReading("Gas Error", "Gas sensor failure", "Gas", false, true, (ReadingSupplier<Boolean>) supplier::isGasError),
        new BooleanSensorReading("CO2-2 Error", "CO2 sensor 2 failure", "CO2-2", false, true, (ReadingSupplier<Boolean>) supplier::isCo2_2Error),
        new BooleanSensorReading("HCHO Error", "Formaldehyde sensor error", "HCHO", false, true, (ReadingSupplier<Boolean>) supplier::isHchoError),
        new BooleanSensorReading("PM Error", "Particulate Matter sensor error", "PM", false, true, (ReadingSupplier<Boolean>) supplier::isPmError),
        new BooleanSensorReading("CO2-1 Error", "CO2 sensor 1 failure", "CO2-1", false, true, (ReadingSupplier<Boolean>) supplier::isCo2_1Error),
        new BooleanSensorReading("Speed Warning", "Fan speed abnormal", "Fan", false, true, (ReadingSupplier<Boolean>) supplier::isSpeedWarning),
        new BooleanSensorReading("Compensation Active", "Compensation enabled", "Sensor", false, true, (ReadingSupplier<Boolean>) supplier::isCompensationActive)
    );
  }
}
