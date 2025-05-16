/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BitsetRegister extends Register {

  private static final String EXCEPTION_NAME = "bitIndex < 0: ";

  private static final int ADDRESS_BITS_PER_WORD = 3; // byte array
  protected byte[] buffer;

  public BitsetRegister(I2CDevice sensor, int address, int size, String name) throws IOException {
    super(sensor, address, name);
    buffer = new byte[size];
    reload();
  }

  private static int wordIndex(int bitIndex) {
    return bitIndex >> ADDRESS_BITS_PER_WORD;
  }

  public static int[] getAllBits(byte[] bytes, boolean set) {
    ArrayList<Integer> bitsList = new ArrayList<>();
    for (int i = 0; i < bytes.length; i++) {
      for (int j = 0; j < 8; j++) {
        boolean isSet = ((bytes[i] & 0xff) & (1 << j)) != 0;
        if (isSet == set) {
          bitsList.add(i * 8 + j);
        }
      }
    }

    // Convert ArrayList to array
    int[] bits = new int[bitsList.size()];
    for (int i = 0; i < bitsList.size(); i++) {
      bits[i] = bitsList.get(i);
    }

    return bits;
  }

  @Override
  protected void reload() throws IOException {
    sensor.readRegister(address, buffer);
  }

  public int[] getAllSet() throws IOException {
    reload();
    boolean hasSet = false;
    for (byte b : buffer) {
      if (b != 0) {
        hasSet = true;
        break;
      }
    }
    if (!hasSet) {
      return new int[0];
    }
    return getAllBits(buffer, true);
  }

  public int[] getAllClear() throws IOException {
    reload();
    return getAllBits(buffer, false);
  }

  public void setAll() throws IOException {
    Arrays.fill(buffer, (byte) 0xff);
    sensor.write(address, buffer);
  }

  public void clearAll() throws IOException {
    Arrays.fill(buffer, (byte) 0x0);
    sensor.write(address, buffer);
  }

  public void flipAll() throws IOException {
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = (byte) ~buffer[i];
    }
    sensor.write(address, buffer);
  }

  public void set(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException(EXCEPTION_NAME + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    int bit = bitIndex % 8;
    buffer[wordIndex] |= (byte) ((1 << bit) & 0xff); // Restores invariants
    sensor.write(address, buffer);
  }

  public void clear(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException(EXCEPTION_NAME + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    int bit = bitIndex % 8;
    buffer[wordIndex] &= (byte) ((1 << bit) & 0xff);
    sensor.write(address, buffer);
  }

  public void flip(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException(EXCEPTION_NAME + bitIndex);

    int wordIndex = wordIndex(bitIndex);
    int bit = bitIndex % 8;
    buffer[wordIndex] ^= (byte) (1 << bit);
    sensor.write(address, buffer);
  }

  public boolean get(int bitIndex) throws IOException {
    if (bitIndex < 0)
      throw new IndexOutOfBoundsException(EXCEPTION_NAME + bitIndex);
    reload();
    int wordIndex = wordIndex(bitIndex);
    int bit = bitIndex % 8;
    return (wordIndex < buffer.length) && ((buffer[wordIndex] & (byte) (1 << bit)) != 0);
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    throw new IOException("Function not applicable");
  }

  @Override
  public String toString(int length) {
    try {
      reload();
    } catch (IOException e) {
      // ignore
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

