package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SetASCERequest extends Request {
  public SetASCERequest(AddressableDevice device) {
    super(1, 0x2416, 0, device);
  }
}
