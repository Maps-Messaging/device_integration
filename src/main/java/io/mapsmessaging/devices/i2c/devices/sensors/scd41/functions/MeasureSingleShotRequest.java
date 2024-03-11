package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class MeasureSingleShotRequest extends Request {
  public MeasureSingleShotRequest(AddressableDevice device) {
    super(5000, 0x219D, 0, device);
  }
}
