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
import io.mapsmessaging.devices.sensorreadings.*;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Getter
public abstract class Sen6xSensor extends I2CDevice implements Sensor, Resetable, PowerManagement {

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

  public Sen6xSensor(AddressableDevice device) {
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

    /*
    String name, String unit, String description, Void example, boolean readOnly, ReadingSupplier<Void> valueSupplier
     */
    GroupSensorReading groupSensorReading = new GroupSensorReading(
        "status",
        "",
        "Device Status Flags",
        null,
        true,
        null);
    groupSensorReading.getGroupList().addAll(buildStatusReadings(new Sen6xStatusSupplier(getDeviceStatusCommand)));
    readings.add(groupSensorReading);
    readings.addAll(buildMeasurementReadingds(contructMeasurementManager(helper)));
    initialise();
    try {
      powerOn();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public static boolean detect(AddressableDevice device) {
    String res = getModel(device);
    return (
        res.equalsIgnoreCase("sen63c")||
        res.equalsIgnoreCase("sen65")||
        res.equalsIgnoreCase("sen66") ||
        res.equalsIgnoreCase("sen68")
    );
  }

  public static String getModel(AddressableDevice device){
    GetProductNameCommand request = new GetProductNameCommand(new Sen6xCommandHelper(device));
    return  request.get();
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
    return "SEN6x";
  }

  @Override
  public String getDescription() {
    return "Air Quality Sensor for PM, RH/T, VOC, Nox, CO2, HCOH";
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

  protected abstract Sen6xMeasurementManager contructMeasurementManager(Sen6xCommandHelper helper);

  protected List<SensorReading<?>> buildMeasurementReadingds(Sen6xMeasurementManager manager) {
    List<SensorReading<?>> tempReadings = new ArrayList<>();

    EnumSet<Sen6xSensorType> supported = SENSOR_SUPPORT_MAP.getOrDefault(productName.trim().toUpperCase(), EnumSet.of(Sen6xSensorType.CO2));

    for (Sen6xSensorType type : supported) {
      switch (type) {
        case CO2 -> tempReadings.add(new Co2MeasurementCommand(manager).asSensorReading());
        case TEMP -> tempReadings.add(new TemperatureMeasurementCommand(manager).asSensorReading());
        case HUMIDITY -> tempReadings.add(new HumidityMeasurementCommand(manager).asSensorReading());
        case VOC -> tempReadings.add(new VocIndexMeasurementCommand(manager).asSensorReading());
        case NOX -> tempReadings.add(new NoxIndexMeasurementCommand(manager).asSensorReading());
        case PM1 -> tempReadings.add(new Pm1_0MeasurementCommand(manager).asSensorReading());
        case PM2_5 -> tempReadings.add(new Pm2_5MeasurementCommand(manager).asSensorReading());
        case PM4 -> tempReadings.add(new Pm4_0MeasurementCommand(manager).asSensorReading());
        case PM10 -> tempReadings.add(new Pm10_0MeasurementCommand(manager).asSensorReading());
        case HCHO -> tempReadings.add(new HchoMeasurementCommand(manager).asSensorReading());
      }
    }
    return tempReadings;
  }

  private static final Map<String, EnumSet<Sen6xSensorType>> SENSOR_SUPPORT_MAP = Map.of(
      "SEN68", EnumSet.of(Sen6xSensorType.HCHO, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC, Sen6xSensorType.NOX, Sen6xSensorType.PM1, Sen6xSensorType.PM2_5, Sen6xSensorType.PM4, Sen6xSensorType.PM10),
      "SEN66", EnumSet.of(Sen6xSensorType.CO2, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC, Sen6xSensorType.NOX, Sen6xSensorType.PM1, Sen6xSensorType.PM2_5, Sen6xSensorType.PM4, Sen6xSensorType.PM10),
      "SEN65", EnumSet.of(Sen6xSensorType.CO2, Sen6xSensorType.HUMIDITY, Sen6xSensorType.TEMP, Sen6xSensorType.VOC),
      "SEN64", EnumSet.of(Sen6xSensorType.CO2)
  );

  private List<OptionalBooleanSensorReading> buildStatusReadings(Sen6xStatusSupplier supplier) {
    return List.of(
        new OptionalBooleanSensorReading("Fan Error", "Fan failure detected", "Fan", false, true, supplier::isFanError),
        new OptionalBooleanSensorReading("RHT Error", "Humidity/Temperature sensor error", "RHT", false, true, supplier::isRhtError),
        new OptionalBooleanSensorReading("Gas Error", "Gas sensor failure", "Gas", false, true, (ReadingSupplier<Boolean>) supplier::isGasError),
        new OptionalBooleanSensorReading("CO₂-2 Error", "CO₂ sensor 2 failure", "CO₂-2", false, true, (ReadingSupplier<Boolean>) supplier::isCo2_2Error),
        new OptionalBooleanSensorReading("HCHO Error", "Formaldehyde sensor error", "HCHO", false, true, (ReadingSupplier<Boolean>) supplier::isHchoError),
        new OptionalBooleanSensorReading("PM Error", "Particulate Matter sensor error", "PM", false, true, (ReadingSupplier<Boolean>) supplier::isPmError),
        new OptionalBooleanSensorReading("CO₂-1 Error", "CO₂ sensor 1 failure", "CO₂-1", false, true, (ReadingSupplier<Boolean>) supplier::isCo2_1Error),
        new OptionalBooleanSensorReading("Speed Warning", "Fan speed abnormal", "Fan", false, true, (ReadingSupplier<Boolean>) supplier::isSpeedWarning),
        new OptionalBooleanSensorReading("Compensation Active", "Compensation enabled", "Sensor", false, true, (ReadingSupplier<Boolean>) supplier::isCompensationActive)
    );
  }
}
