package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ControlHumidity;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.Oversampling;

import java.io.IOException;

public class ControlHumidityRegister extends SingleByteRegister {

  public ControlHumidityRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x72, "Ctrl_hum");
  }

  public Oversampling getHumidityOverSampling() throws IOException {
    reload();
    return Oversampling.values()[registerValue & 0b111];
  }

  public void setHumidityOverSampling(Oversampling mode) throws IOException {
    reload();
    setControlRegister(0b11111000, (mode.getValue() & 0b111));
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ControlHumidity(getHumidityOverSampling());
  }

}
