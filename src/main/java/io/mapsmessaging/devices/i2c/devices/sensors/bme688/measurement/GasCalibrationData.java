/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import lombok.Getter;

@Getter
public class GasCalibrationData {

  private final int parG1;
  private final int parG2;
  private final int parG3;
  private final int heatRange;
  private final int heatVal;
  private final int rangeSwErr;

  public GasCalibrationData(CalibrationData calibrationData) {
    parG1 = calibrationData.getByte(35);
    parG2 = calibrationData.getShort(33);
    parG3 = calibrationData.getByte(36);
    heatRange = (calibrationData.getByte(39) & 0x30) >> 4;
    heatVal = calibrationData.getByte(37);
    rangeSwErr = (calibrationData.getByte(41) & 0xf0) >> 4;
  }


  public int calcResHeat(int desiredTemp, int ambientTemp) {
    double var1;
    double var2;
    double var3;
    double var4;
    double var5;
    int resHeat;

    if (desiredTemp > 400) { // Cap temperature
      desiredTemp = 400;
    }

    // Assuming getters for calibration parameters (parGh1, parGh2, parGh3, ambTemp, resHeatRange, and resHeatVal) are defined in GasCalibrationData
    var1 = ((parG1 / 16.0) + 49.0);
    var2 = (((parG2 / 32768.0) * 0.0005) + 0.00235);
    var3 = (parG3 / 1024.0);
    var4 = (var1 * (1.0 + (var2 * desiredTemp)));
    var5 = (var4 + (var3 * ambientTemp));
    resHeat = (int) (3.4 *
        ((var5 * (4.0 / (4 + heatRange)) *
            (1.0 / (1 + (heatVal * 0.002)))) - 25));

    return resHeat;
  }

}