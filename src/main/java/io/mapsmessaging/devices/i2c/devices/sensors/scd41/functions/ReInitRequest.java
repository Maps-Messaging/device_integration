package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class ReInitRequest extends Request{

  public ReInitRequest(AddressableDevice device){
    super(30, 0x3646, 0, device);
  }

}
