package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetAltitudeRequest;

public class GetAltitudeRegister extends RequestRegister {

  public GetAltitudeRegister(I2CDevice sensor) {
    super(sensor, "getAlititude", new GetAltitudeRequest(sensor.getDevice()));
  }

  public int isDataReady(){
    return ((GetAltitudeRequest)request).getAltitude();
  }

}
