package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SetTempOffsetRequest extends Request {
  public SetTempOffsetRequest(AddressableDevice device) {
    super(1, 0x241D, 0, device);
  }
}
