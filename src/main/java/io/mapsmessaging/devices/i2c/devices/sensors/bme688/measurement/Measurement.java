package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import java.io.IOException;

public interface Measurement {

  double getMeasurement() throws IOException;
}
