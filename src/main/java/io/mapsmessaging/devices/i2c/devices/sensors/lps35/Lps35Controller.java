package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Lps35Controller extends I2CDeviceController {

  private final Lps35Sensor sensor;

  @Getter
  private final String name = "LPS35";
  @Getter
  private final String description = "Pressure and Temperature sensor";


  public Lps35Controller() {
    sensor = null;
  }

  public Lps35Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Lps35Sensor(device);
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
    return (Lps35Sensor.getId(i2cDevice) == 0b10110001);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Lps35Controller(device);
  }


  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    if (sensor != null) {
    }
    return ("{}").getBytes();
  }


  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("temperature", sensor.getTemperature());
      jsonObject.put("pressure", sensor.getPressure());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device LPS35 pressure sensor: 260-1260 hPa");
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
