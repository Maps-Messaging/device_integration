package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.Getter;

@Getter
public class GetAltitudeRequest extends Request {

  private int altitude;

  public GetAltitudeRequest(AddressableDevice device) {
    super(1, 0x2322, 0, device);
  }

  @Override
  public byte[] getResponse(){
    altitude = readValue();
    return new byte[0];
  }
}
