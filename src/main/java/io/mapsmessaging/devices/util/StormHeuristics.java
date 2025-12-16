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


public class StormHeuristics {

  private StormHeuristics() {
  }

  /**
   * @param trendHpaPerHour negative means falling pressure
   * @return hpa per 3 hours (meteorology convention)
   */
  public static float toHpaPer3Hours(float trendHpaPerHour) {
    if (Float.isNaN(trendHpaPerHour) || Float.isInfinite(trendHpaPerHour)) {
      return Float.NaN;
    }
    return trendHpaPerHour * 3.0f;
  }

  public static String describePressureTendency(float trendHpaPer3Hours) {
    if (Float.isNaN(trendHpaPer3Hours)) {
      return "Unknown";
    }
    if (trendHpaPer3Hours <= -0.30f) {
      return "Falling Fast";
    }
    if (trendHpaPer3Hours <= -0.10f) {
      return "Falling";
    }
    if (trendHpaPer3Hours >= 0.30f) {
      return "Rising Fast";
    }
    if (trendHpaPer3Hours >= 0.10f) {
      return "Rising";
    }
    return "Steady";
  }

  /**
   * Simple storm risk heuristic.
   * - falling pressure is the main signal
   * - very low pressure boosts risk a bit
   *
   * @return 0..1 risk score
   */
  public static float stormRisk(float currentPressureHpa, float trendHpaPer3Hours) {
    if (Float.isNaN(currentPressureHpa) || Float.isNaN(trendHpaPer3Hours)) {
      return Float.NaN;
    }

    float fall = -trendHpaPer3Hours;
    float fallScore;
    if (fall <= 0.10f) {
      fallScore = 0.0f;
    } else if (fall >= 0.50f) {
      fallScore = 1.0f;
    } else {
      fallScore = (fall - 0.10f) / (0.50f - 0.10f);
    }

    float lowPressureBoost = 0.0f;
    if (currentPressureHpa <= 99.0f) {
      lowPressureBoost = 0.25f;
    } else if (currentPressureHpa <= 100.0f) {
      lowPressureBoost = 0.10f;
    }

    float risk = fallScore + lowPressureBoost;
    if (risk > 1.0f) {
      risk = 1.0f;
    }
    if (risk < 0.0f) {
      risk = 0.0f;
    }
    return risk;
  }

  public static boolean stormWarning(float currentPressureHpa, float trendHpaPer3Hours) {
    if (Float.isNaN(currentPressureHpa) || Float.isNaN(trendHpaPer3Hours)) {
      return false;
    }

    boolean rapidlyFalling = trendHpaPer3Hours <= -0.30f;
    boolean lowPressure = currentPressureHpa <= 100.0f;
    return rapidlyFalling && lowPressure;
  }
}