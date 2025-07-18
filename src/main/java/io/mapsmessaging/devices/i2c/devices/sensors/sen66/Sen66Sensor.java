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

package io.mapsmessaging.devices.i2c.devices.sensors.sen66;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.sen66.commands.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.BooleanSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class Sen66Sensor extends I2CDevice implements Sensor, Resetable, PowerManagement {

  private final Sen6xCommandHelper helper;

  private GetProductNameCommand getProductNameCommand;
  private GetSerialNumberCommand getSerialNumberCommand;
  private StartMeasurementCommand startMeasurementCommand;
  private StopMeasurementCommand stopMeasurementCommand;
  private SoftResetCommand softResetCommand;
  private GetDeviceStatusCommand getDeviceStatusCommand;
  private GetFirmwareVersionCommand getFirmwareVersionCommand;
  private GetFanCleaningIntervalCommand getFanCleaningIntervalCommand;
  private SetFanCleaningIntervalCommand setFanCleaningIntervalCommand;
  private StartFanCleaningCommand startFanCleaningCommand;
  private ClearDeviceStateCommand clearDeviceStateCommand;
  private final List<SensorReading<?>> readings;

  public Sen66Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Sen66Sensor.class));
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
    readings = List.of(productNameReading, serialNumberReading);
    readings.addAll(buildStatusReadings(new Sen6xStatusSupplier(getDeviceStatusCommand)));
    initialise();
  }


  public static boolean detect(AddressableDevice device) {
    return false;
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
