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
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.PowerMode;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

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
    for(int x=0;x<gasWaitRegisters.length;x++){
      gasWaitRegisters[x] = new GasWaitRegister(this, 0x64 +x, "Gas_wait_"+x);
    }
    heaterResistanceRegister = new HeaterResistanceRegister(this);
    heaterCurrentRegister = new HeaterCurrentRegister(this);
    initialise();
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
    controlMeasurementRegister.setPowerMode(PowerMode.PARALLEL_MODE);
  }

  @Override
  public void powerOff() throws IOException {
    controlMeasurementRegister.setPowerMode(PowerMode.SLEEP_MODE);
  }

  private void initialise() throws IOException {
    resetRegister.reset();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}