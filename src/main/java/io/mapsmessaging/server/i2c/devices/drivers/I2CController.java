package io.mapsmessaging.server.i2c.devices.drivers;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import java.io.IOException;

public class I2CController  {
  protected final I2CBus myBus;
  protected final I2CDevice myDevice;

  public I2CController(int bus, int device) throws IOException, UnsupportedBusNumberException {
    myBus = I2CFactory.getInstance(bus);
    myDevice = myBus.getDevice(device);
  }

  public boolean exists() {
    return true;
  }

  public void close() throws IOException {
    myBus.close();
  }
}