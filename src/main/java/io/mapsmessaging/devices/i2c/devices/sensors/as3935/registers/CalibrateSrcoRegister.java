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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.SrcoCalibrationData;

import java.io.IOException;

public class CalibrateSrcoRegister extends SingleByteRegister {

  private static final int CALIB_SCRO_SRCO_CALIB_TRCO_NOK_BIT = 6;
  private static final int CALIB_SCRO_SRCO_CALIB_SRCO_DONE_BIT = 7;

  public CalibrateSrcoRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x3B, "Calibrate SRCO SRCO");
  }

  public boolean isSRCOCalibrationSuccessful() throws IOException {
    reload();
    return ((registerValue & 0xff) & (1 << CALIB_SCRO_SRCO_CALIB_SRCO_DONE_BIT)) != 0;
  }

  public boolean isSRCOCalibrationUnsuccessful() throws IOException {
    reload();
    return ((registerValue & 0xff) & (1 << CALIB_SCRO_SRCO_CALIB_TRCO_NOK_BIT)) != 0;
  }

  @Override
  public SrcoCalibrationData toData() throws IOException {
    boolean srcoCalibrationSuccessful = isSRCOCalibrationSuccessful();
    boolean srcoCalibrationUnsuccessful = isSRCOCalibrationUnsuccessful();
    return new SrcoCalibrationData(srcoCalibrationSuccessful, srcoCalibrationUnsuccessful);
  }

}