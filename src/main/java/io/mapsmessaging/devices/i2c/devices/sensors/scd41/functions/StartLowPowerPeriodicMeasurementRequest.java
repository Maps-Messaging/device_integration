package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class StartLowPowerPeriodicMeasurementRequest extends Request {
  public StartLowPowerPeriodicMeasurementRequest(AddressableDevice device) {
    super(0, 0x21AC, 0, device);
  }
}
