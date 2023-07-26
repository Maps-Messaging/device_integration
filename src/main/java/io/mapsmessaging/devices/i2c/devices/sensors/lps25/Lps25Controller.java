package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Lps25Controller extends I2CDeviceController {

  private final Lps25Sensor sensor;

  @Getter
  private final String name = "LPS25";
  @Getter
  private final String description = "Pressure and Temperature sensor";


  public Lps25Controller() {
    sensor = null;
  }

  public Lps25Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Lps25Sensor(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor.softReset();
      sensor.setPowerDownMode(true);
      sensor.getControl1().setDataRate(DataRate.RATE_7_HZ);
    }
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return (Lps25Sensor.getId(i2cDevice) == 0b10111101);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Lps25Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device LPS25 pressure sensor: 260-1260 hPa");
    config.setSource("I2C bus address : 0x5d");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing pressure and temperature");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x5D;
    return new int[]{i2cAddr};
  }
}
