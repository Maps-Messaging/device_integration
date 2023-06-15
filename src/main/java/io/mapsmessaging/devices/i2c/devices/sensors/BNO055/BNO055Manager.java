package io.mapsmessaging.devices.i2c.devices.sensors.BNO055;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.io.IOException;


public class BNO055Manager implements I2CDeviceEntry {

  private final int[] i2cAddr = {0x28, 0x29};
  private final BNO055Sensor sensor;

  public BNO055Manager() {
    sensor = null;
  }

  protected BNO055Manager(I2C device) throws IOException {
    sensor = new BNO055Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new BNO055Manager(device);
  }

  public byte[] getPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      double[] orientation = sensor.getOrientation();
      CalibrationStatus status = sensor.getCalibrationStatus();
      SystemStatus system = sensor.getStatus(false);
      jsonObject.put("heading", orientation[0]);
      jsonObject.put("roll", orientation[1]);
      jsonObject.put("pitch", orientation[2]);
      jsonObject.put("isCalibrated", status.isCalibrated());
      jsonObject.put("version", sensor.getVersion());
      jsonObject.put("calibrationStatus", status.toString());
      jsonObject.put("status", system.toString());
      jsonObject.put("state", system.getState().toString());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device BNO055 orientation sensor");
    config.setSource("I2C bus address : 0x28");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}
