package io.mapsmessaging.devices.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import lombok.Getter;

public class Quad7SegmentManager extends HT16K33Manager {

  private final int[] i2cAddr = {0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77};

  @Getter
  private final String name = "Quad 7-Segment LED";

  public Quad7SegmentManager() {
  }

  public Quad7SegmentManager(I2C device) {
    super(new Quad7Segment(device));
  }

  public I2CDeviceEntry mount(I2C device) {
    return new Quad7SegmentManager(device);
  }

  @Override
  public SchemaConfig getSchema() {
    SchemaConfig config = super.getSchema();
    config.setComments("I2C HT16K33 device drives 4 7 segment LEDs with a : in the center");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}