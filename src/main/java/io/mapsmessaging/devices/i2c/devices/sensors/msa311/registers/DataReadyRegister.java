package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.DataReadyData;

import java.io.IOException;

public class DataReadyRegister extends SingleByteRegister {

  public DataReadyRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xA, "Data_Interrupt");
  }

  public boolean isDataReady() throws IOException {
    reload();
    return (registerValue & 0b1) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new DataReadyData(isDataReady());
  }

}
