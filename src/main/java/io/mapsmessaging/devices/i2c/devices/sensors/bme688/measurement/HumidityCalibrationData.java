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
public class HumidityCalibrationData {
  private final int parH1;
  private final int parH2;
  private final int parH3;
  private final int parH4;
  private final int parH5;
  private final int parH6;
  private final int parH7;

  public HumidityCalibrationData(CalibrationData calibrationData) {
    parH1 = (calibrationData.getByte(25) << 4) | (calibrationData.getByte(24) & 0x0F);
    parH2 = (calibrationData.getByte(23) << 4) | (calibrationData.getByte(24) >> 4);
    parH3 = calibrationData.getByte(26);
    parH4 = calibrationData.getByte(27);
    parH5 = calibrationData.getByte(28);
    parH6 = calibrationData.getByte(29);
    parH7 = calibrationData.getByte(30);
  }


}
