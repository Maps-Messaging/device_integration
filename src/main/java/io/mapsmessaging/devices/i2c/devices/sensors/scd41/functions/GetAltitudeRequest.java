package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetAltitudeRequest extends Request {

  public GetAltitudeRequest(AddressableDevice device) {
    super(1, 0x2322, 0, device);
  }

  public int getAltitude(){
    return readValue();
  }
}
