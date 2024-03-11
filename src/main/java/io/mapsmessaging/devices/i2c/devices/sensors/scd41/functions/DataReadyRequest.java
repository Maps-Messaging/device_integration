package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.Getter;

@Getter
public class DataReadyRequest extends Request {

  private boolean dataReady;

  public DataReadyRequest(AddressableDevice device) {
    super(1, 0xE4B8, 0, device);
  }

  @Override
  public byte[] getResponse(){
    dataReady = (readValue() & 0x8000) != 0;
    return new byte[0];
  }
}
