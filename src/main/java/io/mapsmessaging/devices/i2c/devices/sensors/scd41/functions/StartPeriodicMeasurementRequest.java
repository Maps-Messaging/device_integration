package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class StartPeriodicMeasurementRequest extends Request {
  public StartPeriodicMeasurementRequest(AddressableDevice device) {
    super(0, 0x21B1, 0, device); // Assuming no response and immediate execution
  }
}
