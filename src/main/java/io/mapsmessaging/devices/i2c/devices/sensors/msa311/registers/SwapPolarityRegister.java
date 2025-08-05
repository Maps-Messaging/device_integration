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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.SwapPolarityData;

import java.io.IOException;

public class SwapPolarityRegister extends SingleByteRegister {

  private static final byte X_POLARITY = (byte) 0b1000;
  private static final byte Y_POLARITY = (byte) 0b0100;
  private static final byte Z_POLARITY = (byte) 0b0010;
  private static final byte X_Y_SWAP = (byte) 0b0001;

  public SwapPolarityRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x12, "Swap_Polarity");
  }


  public void swapXPolarity(boolean flag) throws IOException {
    int value = flag ? X_POLARITY : 0;
    setControlRegister(~X_POLARITY, value);
  }

  public boolean isXPolaritySwapped() {
    return (registerValue & X_POLARITY) != 0;
  }

  public void swapYPolarity(boolean flag) throws IOException {
    int value = flag ? Y_POLARITY : 0;
    setControlRegister(~Y_POLARITY, value);
  }

  public boolean isYPolaritySwapped() {
    return (registerValue & Y_POLARITY) != 0;
  }

  public void swapZPolarity(boolean flag) throws IOException {
    int value = flag ? Z_POLARITY : 0;
    setControlRegister(~Z_POLARITY, value);
  }

  public boolean isZPolaritySwapped() {
    return (registerValue & Z_POLARITY) != 0;
  }

  public void swapX_Y(boolean flag) throws IOException {
    int value = flag ? X_Y_SWAP : 0;
    setControlRegister(~X_Y_SWAP, value);
  }

  public boolean isX_YSwapped() {
    return (registerValue & X_Y_SWAP) != 0;
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof SwapPolarityData data) {
      swapXPolarity(data.isXPolaritySwapped());
      swapYPolarity(data.isYPolaritySwapped());
      swapZPolarity(data.isZPolaritySwapped());
      swapX_Y(data.isXYSwapped());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new SwapPolarityData(
        isXPolaritySwapped(),
        isYPolaritySwapped(),
        isZPolaritySwapped(),
        isX_YSwapped()
    );
  }
}
