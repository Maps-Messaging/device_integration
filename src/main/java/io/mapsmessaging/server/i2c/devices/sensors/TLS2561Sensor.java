package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.server.i2c.I2CDevice;

public class TLS2561Sensor extends I2CDevice {
  
  private Logger logger = LoggerFactory.getLogger(TLS2561Sensor.class);

  public enum IntegrationTime { ms13_7, ms101, ms402};

  private boolean _initialised;
  private int _full;
  private int _ir;
  private byte _integration;
  private byte _highGain;

  public TLS2561Sensor(I2C device){
    super(device);
    _integration = 0x2;
    _highGain = 0;
    _initialised=false;
    initialise();
  }

  public synchronized void setIntegrationTime(IntegrationTime times, boolean highGain) {
    switch (times){
      case ms13_7:
        _integration = 0;
        break;
      case ms101:
        _integration = 1;
        break;
      case ms402:
        _integration = 2;
        break;

      default:
        _integration = 2;
    }
    if(highGain){
      _highGain = 0x10;
    }
    else{
      _highGain = 0x0;
    }
    write(0x81,(byte)(_integration | _highGain));
    delay(500);
  }

  public void powerOn(){
    write(0x80, (byte)0x03);
    delay(500);
  }

  public void powerOff(){
    write(0x80, (byte)0x0);
  }

  public synchronized boolean initialise()  {
    _initialised = true;
    powerOn();
    setIntegrationTime(IntegrationTime.ms402, false);
    return true;
  }

  public int[] getLevels(){
    scanForChange();
    int[] result = new int[2];
    result[0] = _full;
    result[1] = _ir;
    return result;
  }

  public synchronized void scanForChange() {
    // Read 4 bytes of data
    // ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
    byte[] data=new byte[4];
    readRegister(0x8C, data, 0, 4);
    // Convert the data
    int full = ((data[1] & 0xFF)* 256 + (data[0] & 0xFF));
    int ir   = ((data[3] & 0xFF)* 256 + (data[2] & 0xFF));
    if(full != _full){
      _full = full;
      _ir = ir;
    }
  }

  public int getFull(){
    return _full;
  }

  public int getIr(){
    return _ir;
  }
}