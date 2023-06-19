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
import io.mapsmessaging.devices.spi.SpiDevice;

import java.io.IOException;

public class mcp3y0xDevice extends SpiDevice {

  // SPI device
  protected final int channels;
  protected final int bits;

  public mcp3y0xDevice(Spi spi, int bits, int channels) throws IOException {
    super(spi);
    this.channels = channels;
    this.bits = bits;
  }

  /**
   * Communicate to the ADC chip via SPI to get single-ended conversion value for a specified channel.
   * @param channel analog input channel on ADC chip
   * @return conversion value for specified analog input channel
   * @throws IOException
   */
  public int readFromChannel(short channel) throws IOException {
    if(channel >= channels){
      throw new IOException("Channel count exceeded physical channels");
    }
    // create a data buffer and initialize a conversion request payload
    byte data[] = new byte[] {
        (byte) 0b00000001,                              // first byte, start bit
        (byte)(0b10000000 |( ((channel & 7) << 4))),    // second byte transmitted -> (SGL/DIF = 1, D2=D1=D0=0)
        (byte) 0b00000000                               // third byte transmitted....don't care
    };

    // send conversion request to ADC chip via SPI channel
    spi.write(data);
    byte[] buf = new byte[3];
    spi.read(buf);

    // calculate and return conversion value from result bytes
    int value;
    if(bits == 10){
      value = ((buf[1] & 0b11) << 8);
    }
    else{
      value = ((buf[1] & 0b1111) << 8); //merge data[1] & data[2] to get 12-bit result
    }
    value |=  (buf[2] & 0xff);
    return value;
  }
}
