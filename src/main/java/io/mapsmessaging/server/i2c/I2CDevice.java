package io.mapsmessaging.server.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;

public abstract class I2CDevice implements AutoCloseable {

  @Getter
  private final int busId;
  @Getter
  private final int deviceId;

  protected I2C device;

  public I2CDevice(String name, int bus, int deviceId) {
    busId = bus;
    this.deviceId = deviceId;

    Context pi4j = Pi4J.newAutoContext();
    I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
    I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j).id(name).bus(busId).device(deviceId).build();
    device = i2CProvider.create(i2cConfig);
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