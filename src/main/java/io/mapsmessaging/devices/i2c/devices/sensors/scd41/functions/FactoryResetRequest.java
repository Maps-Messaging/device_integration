package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class FactoryResetRequest extends Request {
  public FactoryResetRequest(AddressableDevice device) {
    super(1200, 0x3632, 0, device); // Assuming no response and immediate execution
  }
}
