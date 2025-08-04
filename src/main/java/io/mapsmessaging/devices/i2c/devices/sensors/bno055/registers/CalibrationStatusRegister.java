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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.data.CalibrationStatusDate;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.CalibrationStatus;

import java.io.IOException;

public class CalibrationStatusRegister extends SingleByteRegister {

  private long lastRead;

  public CalibrationStatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0X35, "Calibration Status");
    lastRead = System.currentTimeMillis();
  }

  public CalibrationStatus getSystem() throws IOException {
    check();
    return getStatus((registerValue >> 6) & 0x3);
  }

  public CalibrationStatus getGryoscope() throws IOException {
    check();
    return getStatus((registerValue >> 4) & 0x3);
  }

  public CalibrationStatus getAccelerometer() throws IOException {
    check();
    return getStatus((registerValue >> 2) & 0x3);
  }

  public CalibrationStatus getMagnetometer() throws IOException {
    check();
    return getStatus(registerValue & 0x3);
  }

  public boolean isCalibrated() throws IOException {
    return getGryoscope() == CalibrationStatus.FULLY_CALIBRATED &&
        getAccelerometer() == CalibrationStatus.FULLY_CALIBRATED &&
        getMagnetometer() == CalibrationStatus.FULLY_CALIBRATED &&
        getSystem() == CalibrationStatus.FULLY_CALIBRATED;
  }

  private CalibrationStatus getStatus(int val) {
    for (CalibrationStatus status : CalibrationStatus.values()) {
      if (status.getMask() == val) {
        return status;
      }
    }
    return CalibrationStatus.UNKNOWN;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new CalibrationStatusDate(
        getName(),
        getSystem(),
        getGryoscope(),
        getAccelerometer(),
        getMagnetometer(),
        isCalibrated()
    );
  }

  private void check() throws IOException {
    if (System.currentTimeMillis() < lastRead) {
      reload();
      lastRead = System.currentTimeMillis() + 100;
    }
  }
}
