package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735.font;

public class FontDef {
  private final int width;
  private final int height;
  private final int[] data;

  public FontDef(int width, int height, int[] data) {
    this.width = width;
    this.height = height;
    this.data = data;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int[] getData() {
    return data;
  }
}
