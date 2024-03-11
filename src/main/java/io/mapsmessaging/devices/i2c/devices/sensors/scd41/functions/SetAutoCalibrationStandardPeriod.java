package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SetAutoCalibrationStandardPeriod extends Request {

  public SetAutoCalibrationStandardPeriod(AddressableDevice device) {
    super(1, 0x244e, 0, device);
  }

  public void setPeriod(int flag){
    setValue(flag);
  }
}
