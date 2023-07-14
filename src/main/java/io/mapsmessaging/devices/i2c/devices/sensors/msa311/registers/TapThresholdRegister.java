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

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Range;
import lombok.Getter;

import java.io.IOException;

public class TapThresholdRegister extends Register {
  private static final byte TAP_TH_MASK = (byte) 0b00011111;

  public TapThresholdRegister(I2CDevice sensor) {
    super(sensor, 0x2B);
  }

  public TapThreshold getTapThreshold(Range range) throws IOException {
    reload();
    float thresholdValue = (float) ((registerValue & TAP_TH_MASK) * range.getLsbMultiplier());
    return new TapThreshold(thresholdValue, range);
  }

  public void setTapThreshold(TapThreshold threshold) throws IOException {
    int maskedValue = (int) (threshold.getValue() / threshold.getRange().getLsbMultiplier()) & TAP_TH_MASK;
    setControlRegister(~TAP_TH_MASK, maskedValue);
  }

  public static class TapThreshold {
    @Getter
    private final float value;
    @Getter
    private final Range range;

    public TapThreshold(float value, Range range) {
      this.value = value;
      this.range = range;
    }
  }
}
