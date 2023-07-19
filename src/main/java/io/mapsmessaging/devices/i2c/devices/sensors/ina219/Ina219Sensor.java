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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers.*;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class Ina219Sensor extends I2CDevice implements Sensor {

  @Getter
  @Setter
  private ADCResolution adcResolution;

  @Getter
  @Setter
  private BusVoltageRange busVoltageRange;

  @Getter
  @Setter
  private GainMask gainMask;

  @Getter
  @Setter
  private OperatingMode operatingMode;

  @Getter
  @Setter
  private ShuntADCResolution shuntADCResolution;

  public Ina219Sensor(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(Ina219Sensor.class));
    adcResolution = ADCResolution.RES_12BIT;
    busVoltageRange = BusVoltageRange.RANGE_32V;
    gainMask = GainMask.GAIN_8_320MV;
    operatingMode = OperatingMode.BVOLT_CONTINUOUS;
    shuntADCResolution = ShuntADCResolution.RES_12BIT_1S_532US;
    setCalibration();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public void setCalibration() throws IOException {
    writeDevice(Registers.CALIBRATION, buildMask());
  }

  public int getBusVoltage() throws IOException {
    int value = readDevice(Registers.BUS_VOLTAGE);
    value = ((value >> 3));
    return value;
  }

  public int getCurrent() throws IOException {
    return readDevice(Registers.CURRENT);
  }

  public int getPower() throws IOException {
    return readDevice(Registers.POWER);
  }

  public int getShuntVoltageRaw() throws IOException {
    return readDevice(Registers.SHUNT_VOLTAGE);
  }

  public double getShuntVoltage() throws IOException {
    int rawValue = getShuntVoltageRaw();
    return rawValue * 0.01;
  }

  public float getCurrent_mA() throws IOException {
    float valueDec = getCurrent();
    return valueDec;
  }

  private int readDevice(Registers register) throws IOException {
    byte[] buf = new byte[2];
    readRegister(register.getAddress(), buf, 0, 2);
    return (buf[0] & 0xff) << 8 | (buf[1] & 0xff);
  }

  private void writeDevice(Registers register, int data) throws IOException {
    byte[] buf = new byte[2];
    buf[0] = (byte) ((data >> 8) & 0xff);
    buf[1] = (byte) (data & 0xff);
    write(register.getAddress(), buf);
  }

  private int buildMask() {
    return
        busVoltageRange.getValue() |
            adcResolution.getValue() |
            operatingMode.getValue() |
            gainMask.getValue() |
            shuntADCResolution.getValue();
  }

  @Override
  public String getName() {
    return "INA219";
  }

  @Override
  public String getDescription() {
    return "Zero-Drift, Bidirectional Current/Power Monitor";
  }

}