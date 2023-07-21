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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.TapThresholdData;

import java.io.IOException;


public class TapThresholdRegister extends SingleByteRegister {
  private static final byte TAP_TH_MASK = (byte) 0b00011111;

  private final RangeRegister rangeRegister;

  public TapThresholdRegister(I2CDevice sensor,  RangeRegister rangeRegister) throws IOException {
    super(sensor, 0x2B, "Tap_Th");
    this.rangeRegister = rangeRegister;
  }

  public float getTapThreshold() throws IOException {
    reload();
    return (float) ((registerValue & TAP_TH_MASK) * rangeRegister.getRange().getLsbMultiplier());
  }

  public void setTapThreshold(float threshold) throws IOException {
    int maskedValue = (int) (threshold / rangeRegister.getRange().getLsbMultiplier()) & TAP_TH_MASK;
    setControlRegister(~TAP_TH_MASK, maskedValue);
  }

  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof TapThresholdData) {
      TapThresholdData data = (TapThresholdData) input;
      int maskedValue = (int) (data.getTapThreshold() / rangeRegister.getRange().getLsbMultiplier()) & TAP_TH_MASK;
      setControlRegister(~TAP_TH_MASK, maskedValue);
      return true;
    }
    return false;
  }

  public AbstractRegisterData toData() throws IOException {
    return new TapThresholdData(getTapThreshold());
  }
}
