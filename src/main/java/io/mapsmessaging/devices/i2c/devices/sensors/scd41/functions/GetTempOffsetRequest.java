package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetTempOffsetRequest extends Request {
  public GetTempOffsetRequest(AddressableDevice device) {
    super(1, 0x2318, 0, device);
  }
}
