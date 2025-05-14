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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Storage;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

public class Lcd1602Device extends I2CDevice implements Output, Storage, Resetable {

  private final ClearDisplay clearDisplay;
  private final DisplayControl displayControl;
  private final CursorControl cursorControl;
  private final CursorHome cursorHome;
  private final EntryModeSet entryModeSet;
  private final FunctionSet functionSet;
  private final SetDdramAddress setDdramAddress;
  private final SetCgramAddress setCgramAddress;

  private byte[] buffer;
  private int cursorPos;

  @Getter
  private int rows;
  @Getter
  private int columns;

  protected Lcd1602Device(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Lcd1602Device.class));
    displayControl = new DisplayControl();
    clearDisplay = new ClearDisplay();
    cursorControl = new CursorControl();
    cursorHome = new CursorHome();
    entryModeSet = new EntryModeSet();
    functionSet = new FunctionSet();
    setDdramAddress = new SetDdramAddress();
    setCgramAddress = new SetCgramAddress();

    rows = 2;
    columns = 16;
    cursorPos = 0;
    buffer = new byte[rows * columns];
    initialise();
  }

  public void initialise() {
    if (rows == 2) {
      functionSet.set2LineDisplay();
    }
    functionSet.set5by10Font();
    displayControl.setDisplayOn(true);
    displayControl.setCursorOn(false);
    displayControl.setBlinkingOn(false);

    sendCommand(functionSet);
    sendCommand(displayControl);
    clearDisplay();
    cursorHome();
    noAutoScroll();
  }

  public void setRows(int rows) {
    if (rows != this.rows) {
      this.rows = rows;
      buffer = new byte[rows * columns];
      if (rows == 2) {
        functionSet.set2LineDisplay();
      }
      if (rows == 1) {
        functionSet.set1LineDisplay();
      }
    }
  }

  public void setColumns(int columns) {
    if (columns != this.columns) {
      this.columns = columns;
      buffer = new byte[rows * columns];
    }
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
    setDisplay(text.getBytes());
  }

  public void setDisplay(byte[] data) {
    for (byte datum : data) {
      writeChar(datum);
    }
  }

  public void customFont(byte location, byte[] charmap) {
    setCgramAddress.setLocation(location);
    sendCommand(setCgramAddress);
    byte[] data = new byte[9];
    data[0] = 0x40;
    System.arraycopy(charmap, 0, data, 1, 8);
    device.write(data);
  }

  public void clearDisplay() {
    sendCommand(clearDisplay);
  }

  public void cursorHome() {
    sendCommand(cursorHome);
    cursorPos = 0;
  }

  public void setCursor(byte row, byte col) {
    setDdramAddress.setCursor(row, col);
    sendCommand(setDdramAddress);
    cursorPos = row * columns + col;
  }

  public void set5by10Font() {
    functionSet.set5by10Font();
    sendCommand(functionSet);
  }

  public void set5by8Font() {
    functionSet.set5by8Font();
    sendCommand(functionSet);
  }

  public void set2LineDisplay() {
    functionSet.set2LineDisplay();
    sendCommand(functionSet);
  }

  public void set1LineDisplay() {
    functionSet.set1LineDisplay();
    sendCommand(functionSet);
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

  private void writeChar(byte data) {
    byte[] buf = new byte[2];
    buf[0] = 0x40;
    buf[1] = data;
    buffer[cursorPos] = data;
    cursorPos++;
    device.write(buf);
  }

  @Override
  public void writeBlock(int address, byte[] data) throws IOException {
    update(address, data, 0);
  }

  public void update(int startPos, byte[] data, int offset) {
    int pos = startPos;
    int remainingData = data.length - offset;
    while (remainingData > 0 && pos < buffer.length) {
      byte row = (byte) (pos / columns);
      byte col = (byte) (pos % columns);
      int bufEnd = (row + 1) * columns;
      int end = Math.min(bufEnd - pos, remainingData);
      setCursor(row, col);
      int idx = offset;
      for (int x = pos; x < pos + end; x++) {
        buffer[x] = data[idx];
        writeChar(buffer[x]);
        idx++;
      }
      pos = bufEnd;
      offset += end;
      remainingData -= end;
    }
  }

  @Override
  public byte[] readBlock(int address, int length) {
    int len = Math.min(buffer.length - address, length);
    byte[] data = new byte[len];
    int x = address;
    int y = 0;
    while (x < buffer.length && y < data.length) {
      data[y] = buffer[x];
      x++;
      y++;
    }
    return data;
  }

  private void sendCommand(Command command) {
    int repeat = command.repeatCount() + 1;
    for (int x = 0; x < repeat; x++) {
      device.write(command.getBuffer());
      delay(command.getCycleTime());
    }
  }

  @Override
  public void reset() throws IOException {
    initialise();
  }

  @Override
  public void softReset() throws IOException {
    clearDisplay();
  }


  @Override
  public DeviceType getType() {
    return DeviceType.DISPLAY;
  }
}
