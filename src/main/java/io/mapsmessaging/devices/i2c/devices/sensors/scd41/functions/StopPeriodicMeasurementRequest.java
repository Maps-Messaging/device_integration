package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class StopPeriodicMeasurementRequest extends Request {
  public StopPeriodicMeasurementRequest(AddressableDevice device) {
    super(500, 0x3F86, 0, device);
  }
}
