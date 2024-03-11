package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class AmbientPressureRequest extends Request {

  public AmbientPressureRequest(AddressableDevice device) {
    super(1, 0xe000, 0, device);
  }

  public int getAmbientPressure(){
    return readValue();
  }

  public void setAmbientPressure(int val){
    setValue(val);
  }
}
