package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SetAltitudeRequest extends Request {
  public SetAltitudeRequest(AddressableDevice device) {
    super(1, 0x2427, 0, device);
  }

  public void setAlititude(int val){
    setValue(val);
  }
}
