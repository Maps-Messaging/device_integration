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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.TrcoCalibrationData;

import java.io.IOException;

public class CalibTrcoRegister extends SingleByteRegister {

  private static final int CALIB_TRCO_NOK_BIT = 0b01000000;
  private static final int CALIB_TRCO_DONE_MASK = 0b10000000;

  public CalibTrcoRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x3A, "Calibrate SRCO TRCO");
  }

  public boolean isTRCOCalibrationSuccessful() throws IOException {
    reload();
    return ((registerValue & 0xff) & CALIB_TRCO_DONE_MASK) != 0;
  }

  public boolean isTRCOCalibrationUnsuccessful() throws IOException {
    reload();
    return ((registerValue & 0xff) & CALIB_TRCO_NOK_BIT) != 0;
  }

  @Override
  public TrcoCalibrationData toData() throws IOException {
    boolean trcoCalibrationSuccessful = isTRCOCalibrationSuccessful();
    boolean trcoCalibrationUnsuccessful = isTRCOCalibrationUnsuccessful();
    return new TrcoCalibrationData(trcoCalibrationSuccessful, trcoCalibrationUnsuccessful);
  }
}