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

public class Constants {
  /*=========================================================================
  CONFIG REGISTER (R/W);
-----------------------------------------------------------------------*/
  public final static int INA219_ADDRESS = (0x40);
  public final static int INA219_READ = (0x01);
  public final static int INA219_REG_CONFIG = (0x00);

  /*---------------------------------------------------------------------*/
  public final static int INA219_CONFIG_RESET = (0x8000);  // Reset Bit

  public final static int INA219_CONFIG_BVOLTAGERANGE_MASK = (0x2000);  // Bus Voltage Range Mask
  public final static int INA219_CONFIG_BVOLTAGERANGE_16V = (0x0000);  // 0-16V Range
  public final static int INA219_CONFIG_BVOLTAGERANGE_32V = (0x2000);  // 0-32V Range

  public final static int INA219_CONFIG_GAIN_MASK =    (0x1800);  // Gain Mask
  public final static int INA219_CONFIG_GAIN_1_40MV =  (0x0000);  // Gain 1, 40mV Range
  public final static int INA219_CONFIG_GAIN_2_80MV =  (0x0800);  // Gain 2, 80mV Range
  public final static int INA219_CONFIG_GAIN_4_160MV = (0x1000);  // Gain 4, 160mV Range
  public final static int INA219_CONFIG_GAIN_8_320MV = (0x1800);  // Gain 8, 320mV Range

  public final static int INA219_CONFIG_BADCRES_MASK =  (0x0780);  // Bus ADC Resolution Mask
  public final static int INA219_CONFIG_BADCRES_9BIT =  (0x0080);  // 9-bit bus res = 0..511
  public final static int INA219_CONFIG_BADCRES_10BIT = (0x0100);  // 10-bit bus res = 0..1023
  public final static int INA219_CONFIG_BADCRES_11BIT = (0x0200);  // 11-bit bus res = 0..2047
  public final static int INA219_CONFIG_BADCRES_12BIT = (0x0400);  // 12-bit bus res = 0..4097

  public final static int INA219_CONFIG_SADCRES_MASK =            (0x0078);  // Shunt ADC Resolution and Averaging Mask
  public final static int INA219_CONFIG_SADCRES_9BIT_1S_84US =    (0x0000);  // 1 x 9-bit shunt sample
  public final static int INA219_CONFIG_SADCRES_10BIT_1S_148US =  (0x0008);  // 1 x 10-bit shunt sample
  public final static int INA219_CONFIG_SADCRES_11BIT_1S_276US =  (0x0010);  // 1 x 11-bit shunt sample
  public final static int INA219_CONFIG_SADCRES_12BIT_1S_532US =  (0x0018);  // 1 x 12-bit shunt sample
  public final static int INA219_CONFIG_SADCRES_12BIT_2S_1060US = (0x0048);   // 2 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_4S_2130US = (0x0050);  // 4 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_8S_4260US = (0x0058);  // 8 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_16S_8510US =(0x0060);  // 16 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_32S_17MS =  (0x0068);  // 32 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_64S_34MS =  (0x0070);  // 64 x 12-bit shunt samples averaged together
  public final static int INA219_CONFIG_SADCRES_12BIT_128S_69MS = (0x0078);  // 128 x 12-bit shunt samples averaged together

  public final static int INA219_CONFIG_MODE_MASK =                (0x0007);  // Operating Mode Mask
  public final static int INA219_CONFIG_MODE_POWERDOWN =           (0x0000);
  public final static int INA219_CONFIG_MODE_SVOLT_TRIGGERED =     (0x0001);
  public final static int INA219_CONFIG_MODE_BVOLT_TRIGGERED =     (0x0002);
  public final static int INA219_CONFIG_MODE_SANDBVOLT_TRIGGERED = (0x0003);
  public final static int INA219_CONFIG_MODE_ADCOFF =              (0x0004);
  public final static int INA219_CONFIG_MODE_SVOLT_CONTINUOUS =    (0x0005);
  public final static int INA219_CONFIG_MODE_BVOLT_CONTINUOUS =    (0x0006);
  public final static int INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS =(0x0007);
  /*=========================================================================*/

  /*=========================================================================*/
  public final static int INA219_REG_SHUNTVOLTAGE = (0x01);
  public final static int INA219_REG_BUSVOLTAGE =   (0x02);
  public final static int INA219_REG_POWER =        (0x03);
  public final static int INA219_REG_CURRENT =      (0x04);
  public final static int INA219_REG_CALIBRATION =  (0x05);
  /*=========================================================================*/


}
