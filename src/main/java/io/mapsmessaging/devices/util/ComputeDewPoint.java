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

public class ComputeDewPoint {

  private static final double MAGNUS_COEFFICIENT = 17.62; // Magnus coefficient
  private static final double MAGNUS_TEMPERATURE_CONSTANT = 243.12; // Magnus temperature constant (in °C)


  private ComputeDewPoint() {
    // no op
  }

  public static double computeDewPoint(double temperature, double humidity) {
    double es = 6.112 * Math.exp((MAGNUS_COEFFICIENT * temperature) / (MAGNUS_TEMPERATURE_CONSTANT + temperature)); // saturation vapor pressure
    double e = (humidity / 100.0) * es;

    double lnRatio = Math.log(e / 6.112);
    return (MAGNUS_TEMPERATURE_CONSTANT * lnRatio) / (MAGNUS_COEFFICIENT - lnRatio);
  }

  public static boolean dewPointWarning(double temperature, double humidity) {
    double dewPoint = computeDewPoint(temperature, humidity);
    double delta = temperature - dewPoint;

    return (delta <= 2.0);
  }
}
