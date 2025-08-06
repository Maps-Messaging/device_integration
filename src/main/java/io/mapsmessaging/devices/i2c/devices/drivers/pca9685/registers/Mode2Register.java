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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.data.Mode2Data;

import java.io.IOException;

public class Mode2Register extends SingleByteRegister {

  private static final int INVRT = 0b00010000;
  private static final int OCH = 0b00001000;
  private static final int OUTDRV = 0b00000100;
  private static final int OUTNE = 0b00000011;

  public Mode2Register(I2CDevice sensor) throws IOException {
    super(sensor, 1, "MODE2");
  }

  public boolean isInvertLogic() {
    return (registerValue & INVRT) != 0;
  }

  public void setInvertLogic(boolean flag) throws IOException {
    setControlRegister(~INVRT, flag ? INVRT : 0);
  }

  public boolean getOutputChangeOnAck() {
    return (registerValue & OCH) != 0;
  }

  public void setOutputChangeOnAck(boolean flag) throws IOException {
    setControlRegister(~OCH, flag ? OCH : 0);
  }

  public boolean getOutputTotemPole() {
    return (registerValue & OUTDRV) != 0;
  }

  public void setOutputTotemPole(boolean flag) throws IOException {
    setControlRegister(~OUTDRV, flag ? OUTDRV : 0);
  }

  public int getOutputDriver() {
    return (registerValue & OUTNE);
  }

  public void setOutputDriver(int flag) throws IOException {
    setControlRegister(~OUTNE, flag & OUTNE);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Mode2Data data) {
      setInvertLogic(data.isInvertLogic());
      setOutputChangeOnAck(data.isOutputChangeOnAck());
      setOutputTotemPole(data.isOutputTotemPole());
      setOutputDriver(data.getOutputDriver());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new Mode2Data(
        isInvertLogic(),
        getOutputChangeOnAck(),
        getOutputTotemPole(),
        getOutputDriver()
    );
  }
}