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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import lombok.Getter;

@Getter
public class PressureCalibrationData {
  private final int parP1;
  private final int parP2;
  private final int parP3;
  private final int parP4;
  private final int parP5;
  private final int parP6;
  private final int parP7;
  private final int parP8;
  private final int parP9;
  private final int parP10;


  public PressureCalibrationData(CalibrationData calibrationData) {
    parP1 = calibrationData.getShort(4);
    parP2 = calibrationData.getShort(6);
    parP3 = calibrationData.getByte(8);
    parP4 = calibrationData.getShort(10);
    parP5 = calibrationData.getShort(12);
    parP6 = calibrationData.getByte(15);
    parP7 = calibrationData.getByte(14);
    parP8 = calibrationData.getByte(18);
    parP9 = calibrationData.getByte(20);
    parP10 = calibrationData.getByte(22);
  }
}
