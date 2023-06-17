package io.mapsmessaging.devices.i2c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.DeviceManager;

import java.io.IOException;

public interface I2CDeviceEntry extends DeviceManager {

  I2CDeviceEntry mount (I2C device) throws IOException;

  int[] getAddressRange();

  boolean detect();

}
