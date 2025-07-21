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

package io.mapsmessaging.devices.i2c.devices.sensors.utils;

public class AqiCalculator {

  public static float computeFromSEN66(float pm2_5, float vocIndex, float noxIndex) {
    float pmAqi = computePm25Aqi(pm2_5);
    float gasAqi = computeGasAqi(vocIndex, noxIndex);
    return Math.max(pmAqi, gasAqi);
  }

  public static float computeFromPMSA003I(float pm2_5, float pm10) {
    float pm25Aqi = computePm25Aqi(pm2_5);
    float pm10Aqi = computePm10Aqi(pm10);
    return Math.max(pm25Aqi, pm10Aqi);
  }

  private static float computePm25Aqi(float value) {
    // US EPA AQI breakpoints for PM2.5
    return computeAqi(value, new float[] {0, 12, 35.4f, 55.4f, 150.4f, 250.4f, 500},
        new int[]   {0, 50, 100, 150, 200, 300, 500});
  }

  private static float computePm10Aqi(float value) {
    // US EPA AQI breakpoints for PM10
    return computeAqi(value, new float[] {0, 54, 154, 254, 354, 424, 604},
        new int[]   {0, 50, 100, 150, 200, 300, 500});
  }

  private static float computeAqi(float value, float[] breakpoints, int[] aqiLevels) {
    for (int i = 0; i < breakpoints.length - 1; i++) {
      if (value <= breakpoints[i + 1]) {
        float clow = breakpoints[i];
        float chigh = breakpoints[i + 1];
        int ilow = aqiLevels[i];
        float ihigh = aqiLevels[i + 1];
        return ilow + (value - clow) * (ihigh - ilow) / (chigh - clow);
      }
    }
    return aqiLevels[aqiLevels.length - 1]; // cap at max
  }

  private static final float[] GAS_BREAKPOINTS = {
      0f, 100f, 200f, 300f, 400f, 500f, Float.MAX_VALUE
  };

  private static final int[] GAS_AQI = {
      0, 50, 100, 150, 200, 300, 500
  };

  private static float computeGasAqi(float vocIndex, float noxIndex) {
    float vocAqi = computeAqi(vocIndex, GAS_BREAKPOINTS, GAS_AQI);
    float noxAqi = computeAqi(noxIndex, GAS_BREAKPOINTS, GAS_AQI);
    return Math.max(vocAqi, noxAqi);
  }

}
