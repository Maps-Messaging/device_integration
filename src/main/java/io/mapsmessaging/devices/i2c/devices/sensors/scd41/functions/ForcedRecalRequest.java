package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class ForcedRecalRequest extends Request {
  public ForcedRecalRequest(AddressableDevice device) {
    super(400, 0x362F, 0, device);
  }
}
