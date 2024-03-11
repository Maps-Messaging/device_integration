package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class PersistSettingsRequest extends Request {
  public PersistSettingsRequest(AddressableDevice device) {
    super(800, 0x3615, 0, device);
  }
}
