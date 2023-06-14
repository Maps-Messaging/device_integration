package io.mapsmessaging.server.i2c;

import com.pi4j.io.i2c.I2C;
import java.util.concurrent.locks.LockSupport;

public abstract class I2CDevice implements AutoCloseable {

  protected I2C device;

  public I2CDevice(I2C device) {
    this.device = device;
  }

  public void close() {
    device.close();
  }

  protected void write(int val){
    device.write(val);
  }

  protected void write(byte[] buffer){
    device.write(buffer, 0, buffer.length);
  }

  protected void write(int register, byte data) {
    device.writeRegister(register, data);
  }

  protected void write(int register, byte[] data) {
    device.writeRegister(register, data);
  }

  protected int read(byte[] buffer){
    return read(buffer, 0, buffer.length);
  }

  protected int read(byte[] buffer, int offset, int length){
    return device.read(buffer, offset, length);
  }

  protected int readRegister(int register){
    return device.readRegister(register);
  }

  protected int readRegister(int register, byte[] output, int offset, int length){
    return device.readRegister(register, output, offset, length);
  }

  protected void delay(int ms){
    LockSupport.parkNanos(ms * 1000000L);
  }
}