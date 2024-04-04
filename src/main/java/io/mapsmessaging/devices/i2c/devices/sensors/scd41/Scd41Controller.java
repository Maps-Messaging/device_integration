package io.mapsmessaging.devices.i2c.devices.sensors.scd41;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Scd41Controller extends I2CDeviceController {

  private final Scd41Sensor sensor;

  @Getter
  private final String name = "SCD-41";
  @Getter
  private final String description = "CO2 Sensor";


  public Scd41Controller() {
    sensor = null;
  }

  public Scd41Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Scd41Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }
  public DeviceType getType(){
    return getDevice().getType();
  }

  @Override
  public boolean canDetect() {
    return sensor.getGetSerialNumberRegister().getSerialNumber() != 0;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return true;
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Scd41Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device SCD-41 CO2 sensor: 400-2000 ppm");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing CO2 levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x62;
    return new int[]{i2cAddr};
  }
}
