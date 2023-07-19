package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Lps25Controller extends I2CDeviceController {

  private final Lps25Sensor sensor;

  @Getter
  private final String name = "LPS25";
  @Getter
  private final String description = "Pressure and Temperature sensor";


  public Lps25Controller() {
    sensor = null;
  }

  public Lps25Controller(I2C device) throws IOException {
    super(device);
    sensor = new Lps25Sensor(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor.softReset();
      sensor.setPowerDownMode(true);
      sensor.getControl1().setDataRate(DataRate.RATE_7_HZ);
    }
  }
  
  public I2CDevice getDevice(){
    return sensor;
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return (Lps25Sensor.getId(i2cDevice) == 0b10111101);
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new Lps25Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, AbstractRegisterData.class);
      List<AbstractRegisterData> data = objectMapper.readValue(new String(val), type);
      sensor.getRegisterMap().setData(data);
    }
    return ("{}").getBytes();
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(sensor.getRegisterMap().getData());
      return json.getBytes();
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      List<SensorReading<?>> readings = sensor.getReadings();
      for(SensorReading<?> reading : readings){
        ComputationResult<?> computationResult = reading.getValue();
        if(!computationResult.hasError()){
          jsonObject.put(reading.getName(), computationResult.getResult());
        }
        else{
          jsonObject.put(reading.getName(), computationResult.getError().getMessage());
        }
      }
    }
    return jsonObject.toString(2).getBytes();
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
