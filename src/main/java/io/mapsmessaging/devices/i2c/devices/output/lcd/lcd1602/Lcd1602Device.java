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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.ClearDisplay;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.Command;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.DisplayControl;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

public class Lcd1602Device extends I2CDevice implements Output {

  private final ClearDisplay clearDisplay;
  private final DisplayControl displayControl;

  protected Lcd1602Device(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Lcd1602Device.class));
    displayControl = new DisplayControl();
    clearDisplay = new ClearDisplay();
  }

  @Override
  public String getName() {
    return "LCD1602";
  }

  @Override
  public String getDescription() {
    return "16 x 2 LCD display";
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  public void clearDisplay() {
    sendCommand(clearDisplay);
  }

  public void setDisplayOn(boolean flag) {
    displayControl.setDisplayOn(flag);
    sendCommand(displayControl);
  }

  public void setCursorOn(boolean flag) {
    displayControl.setCursorOn(flag);
    sendCommand(displayControl);
  }

  public void setBlinkingOn(boolean flag) {
    displayControl.setBlinkingOn(flag);
    sendCommand(displayControl);
  }

  private void sendCommand(Command command) {
    device.write(command.getBuffer());
    delay(2);
  }

}
