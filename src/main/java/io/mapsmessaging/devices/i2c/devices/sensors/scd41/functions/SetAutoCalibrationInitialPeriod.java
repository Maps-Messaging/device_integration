package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SetAutoCalibrationInitialPeriod extends Request {

  public SetAutoCalibrationInitialPeriod(AddressableDevice device) {
    super(1, 0x2445, 0, device);
  }

  public void setPeriod(int flag){
    setValue(flag);
  }
}
