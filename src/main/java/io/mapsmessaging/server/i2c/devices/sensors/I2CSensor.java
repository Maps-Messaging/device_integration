package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class I2CSensor {

  private final int _bus_id;
  private final int _deviceId;

  private final I2CBus _bus;
  protected I2CDevice _device;

  public I2CSensor(int bus, int device) throws IOException {
    _bus_id = bus;
    _deviceId = device;

    try {
      _bus = I2CFactory.getInstance(_bus_id);
      _device = _bus.getDevice(_deviceId);
    } catch (I2CFactory.UnsupportedBusNumberException e) {
      IOException ex = new IOException("Unsupported Bus Number");
      ex.initCause(e);
      throw ex;
    }
  }

  public int getDeviceId() {
    return _deviceId;
  }

  protected void delay(int ms){
    try {
      TimeUnit.MILLISECONDS.sleep(ms);
    } catch (InterruptedException e) {

    }
  }
}