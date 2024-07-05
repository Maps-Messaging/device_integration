package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ControlMeasurement;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.Oversampling;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.PowerMode;

import java.io.IOException;

public class ControlMeasurementRegister extends SingleByteRegister {

  public ControlMeasurementRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x74, "Ctrl_meas");
    reload();
  }

  public PowerMode getMode()  {
    return PowerMode.values()[registerValue & 0b11];
  }

  public void setPowerMode(PowerMode mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b11111100) | (val & 0b11));
  }

  public Oversampling getTemperatureOverSampling() {
    int idx = (0xff & registerValue) >> 5;
    if(idx> Oversampling.values().length){
      return Oversampling.values()[0];
    }
    return Oversampling.values()[idx];
  }

  public void setTemperatureOversampling(Oversampling mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b00011111) | (val & 0b111) << 5);
  }

  public Oversampling getPressureOverSampling() {
    int idx = (registerValue >> 2) & 0b111;
    return Oversampling.values()[idx];
  }

  public void setPressureOversampling(Oversampling mode) {
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b11100011) | (val & 0b111) << 2);
  }

  public void updateRegister() throws IOException {
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ControlMeasurement(getMode(), getTemperatureOverSampling(), getPressureOverSampling());
  }

}
