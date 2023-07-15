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
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Latch;

import java.io.IOException;

public class IntLatchRegister extends SingleByteRegister {

  private static final byte LATCH_MASK = (byte) 0b00001111;
  private static final byte RESET_FLAG = (byte) 0b10000000;

  public IntLatchRegister(I2CDevice sensor) {
    super(sensor, 0x21);
  }

  public Latch getLatch() throws IOException {
    reload();
    int val = registerValue & LATCH_MASK;
    for (Latch latch : Latch.values()) {
      if (latch.getMask() == val) {
        return latch;
      }
    }
    return Latch.NON_LATCHED; // Default
  }

  public void setLatch(Latch latch) throws IOException {
    registerValue = (byte) ((registerValue & ~LATCH_MASK) | latch.getMask());
    sensor.write(address, registerValue);
  }

  public boolean isResetFlagSet() {
    return (registerValue & RESET_FLAG) != 0;
  }

  public void setResetFlag(boolean flag) throws IOException {
    int value = flag ? RESET_FLAG : 0;
    setControlRegister(~RESET_FLAG, value);
  }
}
