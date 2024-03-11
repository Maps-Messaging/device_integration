package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SerialNumberRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.values.SerialNumber;

import java.io.IOException;

public class GetSerialNumberRegister  extends RequestRegister {

  private int serialNumber;
  public GetSerialNumberRegister(I2CDevice sensor) {
    super(sensor, "Get SerialNo", new SerialNumberRequest(sensor.getDevice()));
    serialNumber = -1;
  }

  public int getSerialNumber(){
    if(serialNumber == -1){
      serialNumber = ((SerialNumberRequest)request).getSerialNumber();
    }
    return serialNumber;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new SerialNumber(getSerialNumber());
  }

}
