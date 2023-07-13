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

package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.Sensor;
import io.mapsmessaging.devices.spi.SpiDevice;
import lombok.Getter;

public class Mcp3y0xDevice extends SpiDevice implements Sensor {

  // SPI device
  @Getter
  protected final int channels;
  @Getter
  protected final int bits;
  @Getter
  protected final int dutyCycle = 100000;

  protected final String name;

  public Mcp3y0xDevice(Spi spi, int bits, int channels) {
    super(spi);
    this.channels = channels;
    this.bits = bits;
    name = "MCP3" + (bits == 12 ? "2" : "0") + "0" + (channels == 8 ? "8" : "4");
  }

  /**
   * Communicate to the ADC chip via SPI to get single-ended conversion value for a specified channel.
   *
   * @param channel analog input channel on ADC chip
   * @return conversion value for specified analog input channel
   */
  public int readFromChannel(boolean differential, short channel) {
    if (channel >= channels) {
      return -1;
    }
    byte commandByte;
    if (differential) {
      commandByte = (byte) ((channel & 0b111) << 4);
    } else {
      commandByte = (byte) (0b10000000 | ((channel & 0b111) << 4));
    }
    // create a data buffer and initialize a conversion request payload
    byte[] data = new byte[]{
        (byte) 0b00000001,    // first byte, start bit
        commandByte,          // second byte transmitted -> (SGL/DIF = 1, D2=D1=D0=0)
        (byte) 0b00000000     // third byte transmitted....don't care
    };

    // send conversion request to ADC chip via SPI channel
    byte[] buf = new byte[3];
    transfer(data, buf);

    // calculate and return conversion value from result bytes
    int value;
    if (bits == 10) {
      value = ((buf[1] & 0b0011) << 8);
    } else {
      value = ((buf[1] & 0b1111) << 8); //merge data[1] & data[2] to get 12-bit result
    }
    value |= (buf[2] & 0xff);
    return value;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return "Microchip Technology Analog to Digital " + channels + " channel " + bits + " bit convertor";
  }
}
