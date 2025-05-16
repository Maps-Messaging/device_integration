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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.TapDurData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.TapDuration;

import java.io.IOException;

public class TapDurRegister extends SingleByteRegister {
  private static final byte TAP_QUIET_MASK = (byte) 0b10000000;
  private static final byte TAP_SHOCK_MASK = (byte) 0b01000000;
  private static final byte TAP_DUR_MASK = (byte) 0b00000111;

  public TapDurRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x2A, "Tap_Dur");
  }

  public boolean getTapQuiet() throws IOException {
    reload();
    return (registerValue & TAP_QUIET_MASK) != 0;
  }

  public TapDuration getTapShockDuration() throws IOException {
    reload();
    return TapDuration.values()[(registerValue & TAP_DUR_MASK)];
  }

  public boolean getTapShock() throws IOException {
    reload();
    return (registerValue & TAP_SHOCK_MASK) != 0;
  }

  @Override
  public TapDurData toData() throws IOException {
    return new TapDurData(getTapQuiet(), getTapShockDuration(), getTapShock());
  }

}
