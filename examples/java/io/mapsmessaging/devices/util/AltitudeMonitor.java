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

package io.mapsmessaging.devices.util;

public class AltitudeMonitor {

  private static final double EARTH_GRAVITY = 9.80665;         // m/s^2
  private static final double GAS_CONSTANTS_PER_KG = 287.05;   // (J/(kg·K))
  private static final double GAS_CONSTANTS_PER_MOL = 8.31432; // (J/(mol·K))
  private static final double MOLAR_MASS = 0.02896;            // Molar mass of air (kg/mol)
  private static final double ZERO_CELSIUS_KELVIN = 273.15;

  private final boolean update;
  private float temperature;
  private float pressure;

  public AltitudeMonitor() {
    temperature = Float.NaN;
    pressure = Float.NaN;
    update = true;
  }

  public AltitudeMonitor(float temp, float pressure) {
    temperature = temp;
    this.pressure = pressure;
    update = false;
  }

  public static double computeHeightDifference(double pressure1, double pressure2, double temperature1, double temperature2) {
    double temp1K = temperature1 + ZERO_CELSIUS_KELVIN;  // Convert temperature to Kelvin
    double temp2K = temperature2 + ZERO_CELSIUS_KELVIN;

    double pressure1Pa = pressure1 * 100;  // Convert pressure from hPa to Pa
    double pressure2Pa = pressure2 * 100;
    double val = (2 * EARTH_GRAVITY * MOLAR_MASS);
    if(val != 0.0){
      return ((GAS_CONSTANTS_PER_KG * (temp1K + temp2K) / val) * Math.log(pressure1Pa / pressure2Pa) / 1000.0);
    }
    return 0;
  }

  /**
   * Calculates the height difference based on pressure change and temperature.
   *
   * @param pressure1   The initial pressure measurement in hPa.
   * @param pressure2   The final pressure measurement in hPa.
   * @param temperature The temperature measurement in degrees Celsius.
   * @return The height difference in meters.
   */
  public static double calculateHeightDifference(double pressure1, double pressure2, double temperature) {
    double deltaP = pressure2 - pressure1;
    double deltaT = temperature + 273.15; // Convert temperature to Kelvin

    // Constants for the International Standard Atmosphere (ISA) model
    final double pressure = 1013.25; // Standard pressure at sea level (hPa)
    // Calculate the height difference using the barometric formula
    return (GAS_CONSTANTS_PER_MOL * deltaT) / (MOLAR_MASS * GAS_CONSTANTS_PER_KG) * Math.log((pressure - deltaP) / pressure);
  }

  public double compute(float pressureReading, float temperatureReading) {
    double value = 0;
    value = calculateHeightDifference(pressure, pressureReading, temperatureReading);
    if (update) {
      temperature = temperatureReading;
      pressure = pressureReading;
    }
    return value;
  }

}
