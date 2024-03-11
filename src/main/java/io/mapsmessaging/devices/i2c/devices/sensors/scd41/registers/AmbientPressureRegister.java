package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.AmbientPressureRequest;

public class AmbientPressureRegister extends RequestRegister {

  public AmbientPressureRegister(I2CDevice sensor) {
    super(sensor, "AmbientPressure", new AmbientPressureRequest(sensor.getDevice()));
  }

  public int getPressure() {
    return ((AmbientPressureRequest) request).getAmbientPressure();
  }

  public void setPressure(int val) {
    ((AmbientPressureRequest) request).setAmbientPressure(val);
  }

}
