package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetAutoCalibrationStandardPeriod extends Request {
  public GetAutoCalibrationStandardPeriod(AddressableDevice device) {
    super(1, 0x234b, 0, device);
  }

  public int getPeriod(){
    return readValue();
  }
}