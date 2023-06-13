package io.mapsmessaging.server.i2c;

import io.mapsmessaging.schemas.config.SchemaConfig;
import java.io.IOException;

public interface I2CDeviceEntry {

  I2CDeviceEntry mount (int i2cBusId, int i2cBusAddr) throws IOException;

  SchemaConfig getSchema();

  int[] getAddressRange();
}
