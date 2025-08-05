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
import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;

@Getter
public class MultiByteRegister extends Register {

  protected final byte[] buffer;

  public MultiByteRegister(I2CDevice sensor, int address, int size, String name) {
    super(sensor, address, name);
    buffer = new byte[size];
  }

  @Override
  public void reload() throws IOException {
    Arrays.fill(buffer, (byte) 0);
    sensor.readRegister(address, buffer);
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    throw new IOException("Function not supported");
  }

  protected void write(int val) throws IOException {
    for (int x = buffer.length - 1; x >= 0; x--) {
      buffer[x] = (byte) (val & 0xff);
      val = val >> 8;
    }
    sensor.write(address, buffer);
  }

  protected void write(long val) throws IOException {
    for (int x = buffer.length - 1; x >= 0; x--) {
      buffer[x] = (byte) (val & 0xff);
      val = val >> 8;
    }
    sensor.write(address, buffer);
  }


  protected int asInt() {
    return (int) asLong();
  }


  public long asLongReverse() {
    long val = 0;
    for (int x = 0; x < buffer.length; x++) {
      val = val << 8;
      val |= buffer[x] & 0xff;
    }
    return val;
  }


  public long asLong() {
    long val = 0;
    for (int x = buffer.length - 1; x >= 0; x--) {
      val = val << 8;
      val |= buffer[x] & 0xff;
    }
    return val;
  }

  public long asSignedLong() {
    long val = 0;

    for (int x = buffer.length - 1; x >= 0; x--) {
      val = val << 8;
      if (x == buffer.length - 1) {
        val |= buffer[x];
      } else {
        val |= buffer[x] & 0xff;
      }

    }
    return val;
  }

  @Override
  public String toString(int length) {
    try {
      reload();
    } catch (IOException e) {
      // may fail but can continue
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
