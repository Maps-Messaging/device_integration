package io.mapsmessaging.server.i2c.devices.output.led;

import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import org.json.JSONObject;

public abstract class HT16K33Manager implements I2CDeviceEntry {

  protected final HT16K33Driver display;

  protected HT16K33Manager() {
    this.display = null;
  }

  protected HT16K33Manager(HT16K33Driver display) {
    this.display = display;
  }

  public byte[] getPayload() {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
      jsonObject.put("display", display.getCurrent());
      jsonObject.put("blink", display.isBlinkOn());
      jsonObject.put("blink-fast", display.isFastBlink());
      jsonObject.put("enabled", display.isOn());
      jsonObject.put("brightness", display.getBrightness());
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {
    if (display == null) return;
    JSONObject jsonObject = new JSONObject(new String(val));
    if (jsonObject.has("brightness")) {
      int brightness = jsonObject.getInt("brightness");
      if (brightness != display.getBrightness()) {
        display.setBrightness((byte) (brightness & 0xf));
      }
    }
    if (jsonObject.has("blink")) {
      boolean blink = jsonObject.optBoolean("blink", display.isBlinkOn());
      if (blink != display.isBlinkOn()) {
        display.enableBlink(blink, display.isFastBlink());
      }
    }
    if (jsonObject.has("blink-fast")) {
      boolean fast = jsonObject.optBoolean("blink", display.isFastBlink());
      if (fast != display.isFastBlink()) {
        display.enableBlink(display.isBlinkOn(), fast);
      }
    }

    if (jsonObject.has("enabled")) {
      boolean isOn = jsonObject.optBoolean("enabled", display.isOn());
      if (isOn != display.isOn()) {
        if (isOn) {
          display.turnOn();
        } else {
          display.turnOff();
        }
      }
    }
    String text = jsonObject.getString("display");
    if (text.length() <= 5) {
      display.write(text);
    }
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setSource("I2C bus address configurable from 0x70 to 0x77");
    config.setVersion("1.0");
    config.setResourceType("LED");
    config.setInterfaceDescription("Controls the LED segments");
    return config;
  }
}
