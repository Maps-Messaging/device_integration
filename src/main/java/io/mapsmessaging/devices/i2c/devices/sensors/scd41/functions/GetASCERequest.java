package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetASCERequest extends Request {
  public GetASCERequest(AddressableDevice device) {
    super(1, 0x2313, 0, device);
  }

  public boolean isASCEEnabled(){
    return (readValue() & 0x8000) != 0;
  }
}
