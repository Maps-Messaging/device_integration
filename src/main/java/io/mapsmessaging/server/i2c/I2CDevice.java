package io.mapsmessaging.server.i2c;

import java.io.IOException;

public interface I2CDevice {

  I2CDevice mount (int i2cBusId, int i2cBusAddr) throws IOException;

}
