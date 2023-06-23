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
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class Ina219Sensor extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(Ina219Sensor.class);
  private int ina219_calValue;
  private float ina219_currentDivider_mA;
  private int ina219_powerDivider_mW;

  public Ina219Sensor(I2C device) {
    super(device);
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public void setCalibration_32V_2A() throws IOException {
    ina219_calValue = 4096;
    ina219_currentDivider_mA = 10;    // Current LSB = 100uA per bit (1000/100 = 10)
    ina219_powerDivider_mW = 2;     // Power LSB = 1mW per bit (2/1)
    writeDevice(Constants.INA219_REG_CALIBRATION, ina219_calValue);

    int config =
        Constants.INA219_CONFIG_BVOLTAGERANGE_32V |
            Constants.INA219_CONFIG_GAIN_8_320MV |
            Constants.INA219_CONFIG_BADCRES_12BIT |
            Constants.INA219_CONFIG_SADCRES_12BIT_1S_532US |
            Constants.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS;

    writeDevice(Constants.INA219_REG_CONFIG, config);
  }

  public void setCalibration_32V_1A() throws IOException {
    ina219_calValue = 10240;
    ina219_currentDivider_mA = 25.0f;
    ina219_powerDivider_mW = 1;
    writeDevice(Constants.INA219_REG_CALIBRATION, ina219_calValue);

    int config =
        Constants.INA219_CONFIG_BVOLTAGERANGE_32V |
            Constants.INA219_CONFIG_GAIN_8_320MV |
            Constants.INA219_CONFIG_BADCRES_12BIT |
            Constants.INA219_CONFIG_SADCRES_12BIT_1S_532US |
            Constants.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS;
    writeDevice(Constants.INA219_REG_CONFIG, config);
  }

  public void setCalibration_16V_400mA() throws IOException {
    ina219_calValue = 8192;
    ina219_currentDivider_mA = 20.0f;
    ina219_powerDivider_mW = 1;     // Power LSB = 1mW per bit

    writeDevice(Constants.INA219_REG_CALIBRATION, ina219_calValue);

    // Set Config register to take into account the settings above
    int config =
        Constants.INA219_CONFIG_BVOLTAGERANGE_16V |
            Constants.INA219_CONFIG_GAIN_1_40MV |
            Constants.INA219_CONFIG_BADCRES_12BIT |
            Constants.INA219_CONFIG_SADCRES_12BIT_1S_532US |
            Constants.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS;
    writeDevice(Constants.INA219_REG_CONFIG, config);
  }

  public int getBusVoltage_raw() {
    int value = readDevice(Constants.INA219_REG_BUSVOLTAGE);
    value = ((value >> 3) << 2);
    return value;
  }

  public int getShuntVoltage_raw()  {
    return readDevice(Constants.INA219_REG_SHUNTVOLTAGE);
  }

  public int getCurrent_raw() {
    writeDevice(Constants.INA219_REG_CALIBRATION, ina219_calValue);
    return readDevice(Constants.INA219_REG_CURRENT);
  }

  public float getShuntVoltage_mV() {
    return getShuntVoltage_raw() * 0.01f;
  }

  public float getBusVoltage_V()  {
    return getBusVoltage_raw() * 0.001f;
  }

  public float getCurrent_mA() {
    float valueDec = getCurrent_raw();
    System.out.println("Raw Amp:" + valueDec);
    valueDec /= ina219_currentDivider_mA;
    return valueDec;
  }

  private int readDevice(int command) {
    write((byte) (command & 0xff));
    delay(10);
    int val1 = device.read();
    int val2 = device.read();
    System.out.println("Read : " + Integer.toHexString(val1) + " : " + Integer.toHexString(val2));
    val1 = (val1 & 0xff) << 8;
    return val1 | val2;
  }

  private void writeDevice(int command, int data) {
    write((byte) (command & 0xff));
    write((byte) ((data >> 8) & 0xff));
    write((byte) (data & 0xff));
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