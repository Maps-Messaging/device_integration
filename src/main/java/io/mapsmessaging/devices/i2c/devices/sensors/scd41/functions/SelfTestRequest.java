package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SelfTestRequest extends Request {
  public SelfTestRequest(AddressableDevice device) {
    super(10_000, 0x3639, 0, device); // Adjust responseLength as needed
  }
}
