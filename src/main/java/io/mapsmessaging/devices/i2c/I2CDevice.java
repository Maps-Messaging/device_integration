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

import java.util.concurrent.locks.LockSupport;

public abstract class I2CDevice implements Device, AutoCloseable {

  protected I2C device;

  public I2CDevice(I2C device) {
    this.device = device;
  }

  public void close() {
    device.close();
  }

  public abstract boolean isConnected();

  protected void write(int val) {
    device.write(val);
  }

  protected void write(byte[] buffer) {
    device.write(buffer, 0, buffer.length);
  }

  protected void write(byte[] buffer, int off, int len) {
    device.write(buffer, off, len);
  }

  public void write(int register, byte data) {
    device.writeRegister(register, data);
  }

  public void write(int register, byte[] data) {
    device.writeRegister(register, data);
  }

  protected int read(byte[] buffer) {
    return read(buffer, 0, buffer.length);
  }

  protected int read(byte[] buffer, int offset, int length) {
    return device.read(buffer, offset, length);
  }

  public int readRegister(int register) {
    return device.readRegister(register);
  }

  public int readRegister(int register, byte[] output, int offset, int length) {
    return device.readRegister(register, output, offset, length);
  }

  protected void delay(int ms) {
    LockSupport.parkNanos(ms * 1000000L);
  }
}