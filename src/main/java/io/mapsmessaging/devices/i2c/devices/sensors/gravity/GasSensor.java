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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

public class GasSensor extends I2CDevice {

  @Getter
  private SensorType sensorType;

  private float temperature;
  private int concentration;
  private int decimalPoint;

  private final Logger logger = LoggerFactory.getLogger(GasSensor.class);

  public GasSensor(I2C device) {
    super(device);
    sensorType = detectType();

  }

  public float getTemperature(){
    return temperature;
  }

  public float getConcentration(){
    return concentration;
  }

  public float getTemperatureAdjustedConcentration(){
    if(sensorType != null){
      return sensorType.getSensorModule().computeGasConcentration(temperature, concentration, decimalPoint);
    }
    return 0;
  }

  public void setI2CAddress(int newAddress){

  }
  /**
   * @fn changeAcquireMode
   * @brief Change the mode of acquiring sensor data
   * @param mode Mode select
   * @n     INITIATIVE The sensor proactively reports data
   * @n     PASSIVITY The main controller needs to request data from sensor
   * @return bool type, indicating whether the setting is successful
   * @retval True succeed
   * @retval False failed
   */
  public boolean changeAcquireMode(boolean passivity){
    return false;
  }

  /**
   * @fn setThresholdAlarm
   * @brief Set sensor alarm threshold
   * @param switchof Whether to turn on threshold alarm switch
   * @n            ON turn on
   * @n           OFF turn off
   * @param threshold The threshold for starting alarm
   * @param alamethod Set sensor high or low threshold alarm
   * @param gasType   Gas Type
   * @return bool type, indicating whether the setting is successful
   * @retval True succeed
   * @retval False failed
   */
  public boolean setThresholdAlarm(boolean enable, int threshold, boolean lowThreshold){
   return false;
  }

  /**
   * @fn readTempC
   * @brief Get sensor onboard temperature
   * @return float type, indicating return the current onboard temperature
   */
  public float readTempC(){
    return 0;
  }

  /**
   * @fn setTempCompensation
   * @brief Set whether to turn on temperature compensation, values output by sensor under different temperatures are various.
   * @n     To get more accurate gas concentration, temperature compensation is necessary when calculating gas concentration.
   * @param tempswitch Whether to turn on temperature compensation
   * @n             ON Turn on temperature compensation
   * @n            OFF Turn off temperature compensation
   */
  public void setTempCompensation(boolean flag){

  }

  /**
   * @fn readVolatageData
   * @brief Get sensor gas concentration output by original voltage, which is different from reading sensor register directly.
   * @n     The function is mainly for detecting whether the read gas concentration is right.
   * @param vopin Pin for receiving the original voltage output from sensor probe
   * @return Float type, indicating return the original voltage output of sensor gas concentration
   */
  public float readVolatageData(){
    return 0;
  }
  /**
   * @fn getSensorVoltage
   * @brief Get voltage output by sensor probe (for calculating the current gas concentration)
   * @return float type, indicating return voltage value
   */
  public float getSensorVoltage(){
    return 0.0f;
  }

  public boolean isDataAvailable(){
    return false;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    if(sensorType != null){
      return sensorType.getSku();
    }
    return "GasSensor";
  }

  @Override
  public String getDescription() {
    if(sensorType != null){
      return sensorType.name()+" gas sensor detects from "+sensorType.getMinimumRange()+
          " to "+sensorType.getMaximumRange()+" "+sensorType.getUnits();
    }
    return "Generic Gas Sensor";
  }

  private SensorType detectType(){
    // Code to query the device
    return null;
  }
}