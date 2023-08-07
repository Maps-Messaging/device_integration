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
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

public class Lcd1602Device extends I2CDevice implements Output {

  private final ClearDisplay clearDisplay;
  private final DisplayControl displayControl;
  private final CursorControl cursorControl;
  private final CursorHome cursorHome;
  private final EntryModeSet entryModeSet;

  protected Lcd1602Device(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Lcd1602Device.class));
    displayControl = new DisplayControl();
    clearDisplay = new ClearDisplay();
    cursorControl = new CursorControl();
    cursorHome = new CursorHome();
    entryModeSet = new EntryModeSet();
    initialise();
  }

  public void initialise() {
    clearDisplay();
    cursorHome();
    setDisplayOn(true);
    setCursorOn(true);
    setBlinkingOn(true);
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

  public void setDisplay(String text) {
    cursorHome();
    byte[] buf = new byte[2];
    buf[0] = 0x40;
    byte[] str = text.getBytes();
    int idx = 0;
    for (int x = 0; x < str.length; x++) {
      buf[1] = str[idx];
      idx++;
      device.write(buf);
    }
  }
  public void clearDisplay() {
    sendCommand(clearDisplay);
  }

  public void cursorHome() {
    sendCommand(cursorHome);
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

  public void scrollDisplayLeft() {
    cursorControl.scrollDisplayLeft();
    sendCommand(cursorControl);
  }

  public void scrollDisplayRight() {
    cursorControl.scrollDisplayRight();
    sendCommand(cursorControl);
  }

  public void leftToRight() {
    entryModeSet.leftToRight();
    sendCommand(entryModeSet);
  }

  public void rightToLeft() {
    entryModeSet.rightToLeft();
    sendCommand(entryModeSet);
  }

  public void noAutoScroll() {
    entryModeSet.noAutoScroll();
    sendCommand(entryModeSet);
  }

  public void autoScroll() {
    entryModeSet.autoScroll();
    sendCommand(entryModeSet);
  }

  private void sendCommand(Command command) {
    device.write(command.getBuffer());
    delay(command.getCycleTime());
  }


}
