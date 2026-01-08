/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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


public class AqiCalculator {

  private AqiCalculator() {
    // Hidden
  }

  // -----------------------------
  // PM AQI (EPA-style bands)
  // -----------------------------

  public static String describePmAqi(float aqi) {
    if (Float.isNaN(aqi)) {
      return "Unknown";
    }
    if (aqi <= 50.0f) {
      return "Good";
    }
    if (aqi <= 100.0f) {
      return "Moderate";
    }
    if (aqi <= 150.0f) {
      return "Unhealthy for Sensitive Groups";
    }
    if (aqi <= 200.0f) {
      return "Unhealthy";
    }
    if (aqi <= 300.0f) {
      return "Very Unhealthy";
    }
    return "Hazardous";
  }

  // -----------------------------
  // Indoor GAS score bands
  // -----------------------------

  public static String describeGasScore(float score) {
    if (Float.isNaN(score)) {
      return "Unknown";
    }
    if (score <= 50.0f) {
      return "Fresh";
    }
    if (score <= 100.0f) {
      return "Normal";
    }
    if (score <= 150.0f) {
      return "Noticeable";
    }
    if (score <= 200.0f) {
      return "Stale";
    }
    if (score <= 300.0f) {
      return "Strong";
    }
    return "Very Strong";
  }

  // -----------------------------
  // Existing methods you already have
  // (computePm25Aqi, computePm10Aqi, computePmAqiFromPmsa003I,
  //  computeIndoorGasScoreFromSen66, computeOverallIndoorScore, etc.)
  // -----------------------------

  public static float computeOverallIndoorScore(float pm2_5, float pm10, float vocIndex, float noxIndex) {
    float pmAqi = computePmAqiFromPmsa003I(pm2_5, pm10);
    float gasScore = computeIndoorGasScoreFromSen66(vocIndex, noxIndex);
    return Math.max(pmAqi, gasScore);
  }

  public static float computePmComponent(float pm2_5, float pm10) {
    return computePmAqiFromPmsa003I(pm2_5, pm10);
  }

  public static float computeGasComponent(float vocIndex, float noxIndex) {
    return computeIndoorGasScoreFromSen66(vocIndex, noxIndex);
  }


  public static float computePm25Aqi(float pm2_5) {
    return computeAqi(pm2_5,
        new float[]{0f, 12f, 35.4f, 55.4f, 150.4f, 250.4f, 500f},
        new int[]{0, 50, 100, 150, 200, 300, 500});
  }

  /**
   * Compute PM-based AQI using PM10 only (EPA-style breakpoint interpolation).
   * Caller should provide a suitably averaged PM10 value (not raw instantaneous).
   */
  public static float computePm10Aqi(float pm10) {
    return computeAqi(pm10,
        new float[]{0f, 54f, 154f, 254f, 354f, 424f, 604f},
        new int[]{0, 50, 100, 150, 200, 300, 500});
  }

  /**
   * PM-based AQI from PMSA003I (uses PM2.5 and PM10; returns worst-case).
   */
  public static float computePmAqiFromPmsa003I(float pm2_5, float pm10) {
    float pm25Aqi = computePm25Aqi(pm2_5);
    float pm10Aqi = computePm10Aqi(pm10);
    return Math.max(pm25Aqi, pm10Aqi);
  }

  /**
   * Indoor GAS score (NOT AQI): maps VOC/NOx indices to a 0..500 severity score.
   * This is a heuristic score intended for indoor "gasiness", not regulatory AQI.
   *
   * Bands are intentionally simple and trend-friendly:
   * - VOC: 0-100 ok, 100-200 elevated, 200-300 high, 300-400 very high, 400+ extreme
   * - NOx: 0-10 low, 10-50 elevated, 50-150 high, 150-300 very high, 300+ extreme
   *
   * Returned score uses the same 0..500 range for easy UI alignment, but is NOT AQI.
   */
  public static float computeIndoorGasScoreFromSen66(float vocIndex, float noxIndex) {
    float vocScore = mapVocIndexToScore(vocIndex);
    float noxScore = mapNoxIndexToScore(noxIndex);
    return Math.max(vocScore, noxScore);
  }


  private static float computeAqi(float value, float[] breakpoints, int[] aqiLevels) {
    for (int i = 0; i < breakpoints.length - 1; i++) {
      float upper = breakpoints[i + 1];
      if (value <= upper) {
        float concentrationLow = breakpoints[i];
        float concentrationHigh = upper;
        int aqiLow = aqiLevels[i];
        int aqiHigh = aqiLevels[i + 1];
        return aqiLow + (value - concentrationLow) * (aqiHigh - aqiLow) / (concentrationHigh - concentrationLow);
      }
    }
    return aqiLevels[aqiLevels.length - 1];
  }

  private static float mapVocIndexToScore(float vocIndex) {
    if (vocIndex <= 0.0f) {
      return 0.0f;
    }
    if (vocIndex <= 100.0f) {
      return scaleLinear(vocIndex, 0.0f, 100.0f, 0.0f, 100.0f);
    }
    if (vocIndex <= 200.0f) {
      return scaleLinear(vocIndex, 100.0f, 200.0f, 100.0f, 200.0f);
    }
    if (vocIndex <= 300.0f) {
      return scaleLinear(vocIndex, 200.0f, 300.0f, 200.0f, 300.0f);
    }
    if (vocIndex <= 400.0f) {
      return scaleLinear(vocIndex, 300.0f, 400.0f, 300.0f, 400.0f);
    }
    if (vocIndex <= 500.0f) {
      return scaleLinear(vocIndex, 400.0f, 500.0f, 400.0f, 500.0f);
    }
    return 500.0f;
  }

  private static float mapNoxIndexToScore(float noxIndex) {
    if (noxIndex <= 0.0f) {
      return 0.0f;
    }
    if (noxIndex <= 10.0f) {
      return scaleLinear(noxIndex, 0.0f, 10.0f, 0.0f, 100.0f);
    }
    if (noxIndex <= 50.0f) {
      return scaleLinear(noxIndex, 10.0f, 50.0f, 100.0f, 200.0f);
    }
    if (noxIndex <= 150.0f) {
      return scaleLinear(noxIndex, 50.0f, 150.0f, 200.0f, 300.0f);
    }
    if (noxIndex <= 300.0f) {
      return scaleLinear(noxIndex, 150.0f, 300.0f, 300.0f, 400.0f);
    }
    if (noxIndex <= 500.0f) {
      return scaleLinear(noxIndex, 300.0f, 500.0f, 400.0f, 500.0f);
    }
    return 500.0f;
  }

  private static float scaleLinear(float value, float inputLow, float inputHigh, float outputLow, float outputHigh) {
    if (value <= inputLow) {
      return outputLow;
    }
    if (value >= inputHigh) {
      return outputHigh;
    }
    return outputLow + (value - inputLow) * (outputHigh - outputLow) / (inputHigh - inputLow);
  }
}