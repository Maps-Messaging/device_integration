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
  }

  public PowerMode getMode() throws IOException {
    reload();
    return PowerMode.values()[registerValue & 0b11];
  }

  public void setPowerMode(PowerMode mode) throws IOException {
    reload();
    int val = mode.getValue();
    registerValue = (byte) ((registerValue & 0b11111100) | (val & 0b11));
    setControlRegister(0b11111100, (val & 0b11));
  }

  public Oversampling getTemperatureOverSampling() throws IOException {
    reload();
    int idx = registerValue >> 5;
    return Oversampling.values()[idx];
  }

  public void setTemperatureOversampling(Oversampling mode) throws IOException {
    reload();
    int val = mode.getValue();
    setControlRegister(0b00011111, (val & 0b111) << 5);
  }

  public Oversampling getPressureOverSampling() throws IOException {
    reload();
    int idx = (registerValue >> 2) & 0b111;
    return Oversampling.values()[idx];
  }

  public void setPressureOversampling(Oversampling mode) throws IOException {
    reload();
    int val = mode.getValue();
    setControlRegister(0b11100011, (val & 0b111) << 2);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ControlMeasurement(getMode(), getTemperatureOverSampling(), getPressureOverSampling());
  }

}
