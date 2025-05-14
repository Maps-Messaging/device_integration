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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.backlight;

import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

public abstract class BacklightPwm extends I2CDevice implements Output {

  private static final byte REG_MODE1 = 0x00;
  private static final byte REG_MODE2 = 0x01;
  private static final byte REG_OUTPUT = 0x08;

  private final byte regRed;
  private final byte regGreen;
  private final byte regBlue;
  private final byte regOnly;

  protected BacklightPwm(AddressableDevice device, byte regRed, byte regGreen, byte regBlue, byte regOnly) {
    super(device, LoggerFactory.getLogger(BacklightPwm.class));
    this.regGreen = regGreen;
    this.regBlue = regBlue;
    this.regRed = regRed;
    this.regOnly = regOnly;
    initialise();
  }

  public void initialise() {
    sendCommand(REG_MODE1, (byte) 0);
    sendCommand(REG_OUTPUT, (byte) 0xFF);
    sendCommand(REG_MODE2, (byte) 0x20);
    sendCommand(regRed, (byte) 0x77);
    sendCommand(regGreen, (byte) 0x77);
    sendCommand(regBlue, (byte) 0x77);
  }


  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public String getName() {
    return "BacklightPwm";
  }

  @Override
  public String getDescription() {
    return "Backlight control";
  }

  protected void sendCommand(byte addr, byte val) {
    device.writeRegister(addr, new byte[]{val});
  }

}