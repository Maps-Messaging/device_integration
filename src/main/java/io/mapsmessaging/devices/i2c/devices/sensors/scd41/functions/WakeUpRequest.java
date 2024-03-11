package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class WakeUpRequest extends Request {
  public WakeUpRequest(AddressableDevice device) {
    super(30, 0x36f6, 0, device);
  }
}
