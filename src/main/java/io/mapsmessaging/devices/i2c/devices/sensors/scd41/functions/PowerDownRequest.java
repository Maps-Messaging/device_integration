package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class PowerDownRequest extends Request {
  public PowerDownRequest(AddressableDevice device) {
    super(1, 0x36e0, 0, device);
  }
}
