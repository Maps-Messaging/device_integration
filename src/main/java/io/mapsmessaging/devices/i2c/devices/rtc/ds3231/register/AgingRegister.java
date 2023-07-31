package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.AgingData;

import java.io.IOException;

public class AgingRegister extends SingleByteRegister {

  public AgingRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x10, "AGING");
  }

  public int getAging() throws IOException{
    reload();
    return registerValue;
  }

  public void setAging(int aging) throws IOException {
    sensor.write(address, (byte) aging);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof AgingData) {
      AgingData data = (AgingData) input;
      setAging(data.getAging());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new AgingData(getAging());
  }
}