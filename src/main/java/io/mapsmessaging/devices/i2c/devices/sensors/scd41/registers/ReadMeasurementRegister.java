package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ReadMeasurementRequest;

public class ReadMeasurementRegister extends RequestRegister {

  private long lastRead;

  public ReadMeasurementRegister(I2CDevice sensor) {
    super(sensor, "get measurement", new ReadMeasurementRequest(sensor.getDevice()));
    lastRead = 0;
  }

  public boolean hasData(){
    if(lastRead<System.currentTimeMillis()) {
      request.getResponse();
      lastRead = System.currentTimeMillis() + 5000;
      return true;
    }
    return false;
  }

  public int getCo2(){
    hasData();
    return ((ReadMeasurementRequest)request).getCo2();
  }
  public float getHumidity(){
    hasData();
    return ((ReadMeasurementRequest)request).getHumidity();
  }
  public float getTemperature(){
    hasData();
    return ((ReadMeasurementRequest)request).getTemperature();
  }

}
