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

package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Storage;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.output.lcd.st7735.font.FontDef;
import io.mapsmessaging.devices.i2c.devices.output.lcd.st7735.font.Fonts;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class St7735Device extends I2CDevice implements Output, Storage, Resetable {
  // Color definitions
  public static final int ST7735_BLACK = 0x0000;
  public static final int ST7735_BLUE = 0x001F;
  public static final int ST7735_RED = 0xF800;
  public static final int ST7735_GREEN = 0x07E0;
  public static final int ST7735_CYAN = 0x07FF;
  public static final int ST7735_MAGENTA = 0xF81F;
  public static final int ST7735_YELLOW = 0xFFE0;
  public static final int ST7735_WHITE = 0xFFFF;
  public static final int ST7735_GRAY = 0x8410;
  private static final int I2C_ADDRESS = 0x18;
  private static final int BURST_MAX_LENGTH = 160;
  private static final int X_COORDINATE_MAX = 160;
  private static final int X_COORDINATE_MIN = 0;
  private static final int Y_COORDINATE_MAX = 80;
  private static final int Y_COORDINATE_MIN = 0;
  private static final int X_COORDINATE_REG = 0x2A;
  private static final int Y_COORDINATE_REG = 0x2B;
  private static final int CHAR_DATA_REG = 0x2C;
  private static final int SCAN_DIRECTION_REG = 0x36;
  private static final int WRITE_DATA_REG = 0x00;
  private static final int BURST_WRITE_REG = 0x01;
  private static final int SYNC_REG = 0x03;
  private static final int ST7735_MADCTL_MY = 0x80;
  private static final int ST7735_MADCTL_MX = 0x40;
  private static final int ST7735_MADCTL_MV = 0x20;
  private static final int ST7735_MADCTL_ML = 0x10;
  private static final int ST7735_MADCTL_RGB = 0x00;
  private static final int ST7735_MADCTL_BGR = 0x08;
  private static final int ST7735_MADCTL_MH = 0x04;
  private static final int ST7735_IS_160X80 = 1;
  private static final int ST7735_XSTART = 0;
  private static final int ST7735_YSTART = 24;
  private static final int ST7735_WIDTH = 160;
  private static final int ST7735_HEIGHT = 80;
  private static final int ST7735_ROTATION = ST7735_MADCTL_MY | ST7735_MADCTL_MV | ST7735_MADCTL_BGR;

  public St7735Device(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(St7735Device.class));
    reset();
  }

  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x18, "ST7735");
    if (deviceController != null) {
      System.err.println(new String(deviceController.getDeviceConfiguration()));
      I2CDevice sensor = deviceController.getDevice();
      Random random = new Random(System.currentTimeMillis());

      if (sensor instanceof St7735Device) {
        St7735Device device1 = (St7735Device) sensor;
        device1.reset();
        while (true) {
          device1.lcdDisplayPercentage("Test:", Math.abs(random.nextInt(100)), 100);
          device1.delay(1000);
        }
      }
    }
  }

  public void lcdSetAddressWindow(int x0, int y0, int x1, int y1) throws IOException {
    i2cWriteCommand(X_COORDINATE_REG, x0 + ST7735_XSTART, x1 + ST7735_XSTART);
    i2cWriteCommand(Y_COORDINATE_REG, y0 + ST7735_YSTART, y1 + ST7735_YSTART);
    i2cWriteCommand(CHAR_DATA_REG, 0x00, 0x00);
    i2cWriteCommand(SYNC_REG, 0x00, 0x01);
  }

  public void lcdWriteChar(int x, int y, char ch, FontDef font, int color, int bgcolor) throws IOException {
    lcdSetAddressWindow(x, y, x + font.getWidth() - 1, y + font.getHeight() - 1);
    for (int i = 0; i < font.getHeight(); i++) {
      int b = font.getData()[(ch - 32) * font.getHeight() + i];
      for (int j = 0; j < font.getWidth(); j++) {
        if (((b << j) & 0x8000) != 0) {
          i2cWriteData(color >> 8, color & 0xFF);
        } else {
          i2cWriteData(bgcolor >> 8, bgcolor & 0xFF);
        }
      }
    }
  }

  public void lcdWriteString(int x, int y, String str, FontDef font, int color, int bgcolor) throws IOException {
    for (char ch : str.toCharArray()) {
      if (x + font.getWidth() >= ST7735_WIDTH) {
        x = 0;
        y += font.getHeight();
        if (y + font.getHeight() >= ST7735_HEIGHT) {
          break;
        }
      }
      lcdWriteChar(x, y, ch, font, color, bgcolor);
      i2cWriteCommand(SYNC_REG, 0x00, 0x01);
      x += font.getWidth();
    }
  }

  public void lcdFillRectangle(int x, int y, int w, int h, int color) throws IOException {
    if (x >= ST7735_WIDTH || y >= ST7735_HEIGHT) return;
    if (x + w - 1 >= ST7735_WIDTH) w = ST7735_WIDTH - x;
    if (y + h - 1 >= ST7735_HEIGHT) h = ST7735_HEIGHT - y;

    lcdSetAddressWindow(x, y, x + w - 1, y + h - 1);

    byte[] buff = new byte[w * 2];
    for (int i = 0; i < w; i++) {
      buff[i * 2] = (byte) (color >> 8);
      buff[i * 2 + 1] = (byte) (color & 0xFF);
    }
    for (int i = 0; i < h; i++) {
      i2cBurstTransfer(buff, buff.length);
    }
  }

  public void lcdFillScreen(int color) throws IOException {
    lcdFillRectangle(0, 0, ST7735_WIDTH, ST7735_HEIGHT, color);
    i2cWriteCommand(SYNC_REG, 0x00, 0x01);
  }

  public void lcdDrawImage(int x, int y, int w, int h, byte[] data) throws IOException {
    lcdSetAddressWindow(x, y, x + w - 1, y + h - 1);
    i2cBurstTransfer(data, data.length);
  }

  public void i2cWriteData(int high, int low) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(3);
    buffer.put((byte) WRITE_DATA_REG);
    buffer.put((byte) high);
    buffer.put((byte) low);
    device.write(buffer.array());
  }

  public void i2cWriteCommand(int command, int high, int low) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(3);
    buffer.put((byte) command);
    buffer.put((byte) high);
    buffer.put((byte) low);
    device.write(buffer.array());
  }

  public void i2cBurstTransfer(byte[] buff, int length) throws IOException {
    i2cWriteCommand(BURST_WRITE_REG, 0x00, 0x01);
    int count = 0;
    while (length > count) {
      int chunkLength = Math.min(length - count, BURST_MAX_LENGTH);
      device.write(buff, count, chunkLength);
      count += chunkLength;
      delay(1);
    }
    i2cWriteCommand(BURST_WRITE_REG, 0x00, 0x00);
    i2cWriteCommand(SYNC_REG, 0x00, 0x01);
  }

  public void lcdDisplayPercentage(int val, int color) throws IOException {
    val += 10;
    if (val >= 100) val = 100;
    val /= 10;

    int xCoordinate = 30;
    for (int i = 0; i < val; i++) {
      lcdFillRectangle(xCoordinate, 60, 6, 10, color);
      xCoordinate += 10;
    }
    for (int i = val; i < 10; i++) {
      lcdFillRectangle(xCoordinate, 60, 6, 10, ST7735_GRAY);
      xCoordinate += 10;
    }
  }

  public void lcdDisplayValue(String name, int value) throws IOException {
    lcdFillScreen(ST7735_BLACK);
    lcdFillRectangle(0, 20, ST7735_WIDTH, 5, ST7735_BLUE);
    lcdWriteString(0, 0, name + value + "%", Fonts.FONT_8X16, ST7735_WHITE, ST7735_BLACK);
    lcdDisplayPercentage(value, ST7735_GREEN);
  }

  public void lcdDisplayPercentage(String name, int val, int max) throws IOException {
    int usedDiskPercentage = (int) ((float) val / max * 100);
    lcdFillRectangle(0, 35, ST7735_WIDTH, 20, ST7735_BLACK);
    lcdWriteString(30, 35, name + usedDiskPercentage + "%", Fonts.FONT_11X18, ST7735_WHITE, ST7735_BLACK);
    lcdDisplayPercentage(usedDiskPercentage, ST7735_BLUE);
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public String getName() {
    return "ST7735 LCD Panel";
  }

  @Override
  public String getDescription() {
    return "LCD Panel";
  }

  @Override
  public void reset() throws IOException {
    lcdFillScreen(0);
  }

  @Override
  public void softReset() throws IOException {
    lcdFillScreen(0);
  }

  @Override
  public DeviceType getType() {
    return DeviceType.DISPLAY;
  }

  @Override
  public void writeBlock(int address, byte[] data) throws IOException {

  }

  @Override
  public byte[] readBlock(int address, int length) throws IOException {
    return new byte[0];
  }
}
