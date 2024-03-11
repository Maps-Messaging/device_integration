package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class MeasureSingleShotRHOnlyRequest extends Request {
  public MeasureSingleShotRHOnlyRequest(AddressableDevice device) {
    super(50, 0x2196, 0, device);
  }
}
