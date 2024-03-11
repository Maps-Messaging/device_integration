package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class GetAmbientPressureRequest extends Request {

  private int pressure;

  public GetAmbientPressureRequest(AddressableDevice device) {
    super(1, 0xe000, 0, device);
  }

  @Override
  public byte[] getResponse() {
    pressure = readValue();
    return new byte[0];
  }
}
