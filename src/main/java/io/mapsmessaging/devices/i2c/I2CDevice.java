/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.Logger;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.*;

public abstract class I2CDevice implements Device, AutoCloseable {

  protected I2C device;
  protected final Logger logger;

  protected I2CDevice(I2C device, Logger logger) {
    this.device = device;
    this.logger = logger;
    logger.log(I2C_BUS_DEVICE_ALLOCATED);
  }

  public void close() {
    device.close();
    log(I2C_BUS_DEVICE_CLOSE);
  }

  public abstract boolean isConnected();

  protected void write(int val) {
    if (logger.isDebugEnabled()) {
      log(I2C_BUS_DEVICE_WRITE, 0, String.format("%02X", val));
    }
    device.write(val);
  }

  protected void write(byte[] buffer) {
    write(buffer, 0, buffer.length);
  }

  protected void write(byte[] buffer, int off, int len) {
    device.write(buffer, off, len);
    if (logger.isDebugEnabled()) {
      String bufferString = dump(buffer, buffer.length);
      log(I2C_BUS_DEVICE_WRITE, 0, bufferString);
    }
  }

  public void write(int register, byte data) {
    if (logger.isDebugEnabled()) {
      log(I2C_BUS_DEVICE_WRITE, register, String.format("%02X", data));
    }
    device.writeRegister(register, data);
  }

  public void write(int register, byte[] data) {
    if (logger.isDebugEnabled()) {
      String bufferString = dump(data, data.length);
      log(I2C_BUS_DEVICE_WRITE, register, bufferString);
    }
    device.writeRegister(register, data);
  }

  protected int read(byte[] buffer) {
    return read(buffer, 0, buffer.length);
  }

  protected int read(byte[] buffer, int offset, int length) {
    int read = device.read(buffer, offset, length);
    if (logger.isDebugEnabled()) {
      String bufferString = dump(buffer, read);
      log(I2C_BUS_DEVICE_READ, 0, bufferString);
    }
    return read;
  }

  public int readRegister(int register) {
    int val = device.readRegister(register);
    if (logger.isDebugEnabled()) {
      log(I2C_BUS_DEVICE_READ, register, String.format("%02X", val));
    }
    return val;
  }

  public int readRegister(int register, byte[] output) {
    return readRegister(register, output, 0, output.length);
  }

  public int readRegister(int register, byte[] output, int offset, int length) {
    int read = device.readRegister(register, output, offset, length);
    if (logger.isDebugEnabled()) {
      String bufferString = dump(output, read);
      log(I2C_BUS_DEVICE_READ, register, bufferString);
    }
    return read;
  }

  @Override
  public void delay(int ms) {
    try {
      log(I2C_BUS_DEVICE_DELAY, ms);
      //this will allow other devices access to the I2C bus
      I2CDeviceScheduler.getI2cBusLock().wait(ms);
    } catch (InterruptedException e) {
      // Ignore the interrupt
      Thread.currentThread().interrupt(); // Pass it up
    }
  }

  private void log(DeviceLogMessage message, Object... args){
    logger.log(message, device.getBus(), device.getDevice(),  args);
  }
}