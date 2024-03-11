package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetAutoCalibrationInitialPeriod extends Request {
  public GetAutoCalibrationInitialPeriod(AddressableDevice device) {
    super(1, 0x2340, 0, device);
  }

  public int getPeriod(){
    return readValue();
  }
}