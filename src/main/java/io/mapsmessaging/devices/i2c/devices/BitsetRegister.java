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

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class BitsetRegister extends Register {

  private static final int ADDRESS_BITS_PER_WORD = 3; // byte array
  protected byte[] buffer;

  public BitsetRegister(I2CDevice sensor, int address, int size, String name) throws IOException {
    super(sensor, address, name);
    buffer = new byte[size];
    reload();
  }

  @Override
  protected void reload() throws IOException {
    sensor.readRegister(address, buffer);
  }

  public void set(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    buffer[wordIndex] |= (byte) ((1 << bitIndex) & 0xff); // Restores invariants
    sensor.write(address, buffer);
  }

  public void clear(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    buffer[wordIndex] &= (byte) (~(1 << bitIndex) & 0xff);
    sensor.write(address, buffer);
  }

  public void flip(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    buffer[wordIndex] ^= (1L << bitIndex);
    sensor.write(address, buffer);
  }

  public boolean get(int bitIndex) {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    int wordIndex = wordIndex(bitIndex);
    return (wordIndex < buffer.length) && ((buffer[wordIndex] & (byte) (1 << bitIndex)) != 0);
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
  }

  private static int wordIndex(int bitIndex) {
    return bitIndex >> ADDRESS_BITS_PER_WORD;
  }

  @Override
  public String toString(int length) {
    try {
      reload();
    } catch (IOException e) {

    }
    StringBuilder stringBuilder = new StringBuilder();
    int c = 0;
    for (byte b : buffer) {
      if (c != 0) stringBuilder.append("\t");
      stringBuilder.append(displayRegister(length, getAddress() + c, b));
      c++;
      if (c < buffer.length) stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }
}

