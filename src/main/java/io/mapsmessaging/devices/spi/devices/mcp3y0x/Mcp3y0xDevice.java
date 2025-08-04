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

package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.spi.SpiDevice;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Mcp3y0xDevice extends SpiDevice implements Sensor {

  // SPI device
  protected final int channels;
  @Getter
  protected final int bits;
  @Getter
  protected static final int dutyCycle = 100000;

  protected final String name;
  @Getter
  private final List<SensorReading<?>> readings;

  public Mcp3y0xDevice(Spi spi, int bits, int channels) {
    super(spi);
    this.channels = channels;
    this.bits = bits;

    name = "MCP3" + (bits == 12 ? "2" : "0") + "0" + (channels == 8 ? "8" : "4");
    readings = new ArrayList<>();

    int max = (1 << (bits + 1)) - 1;

    for (short x = 0; x < channels; x++) {
      readings.add(new IntegerSensorReading(
          "channel_" + x,
          "digital",
          "Raw digital reading from MCP3 ADC channel " + x,
          0,
          true,
          0,
          max,
          new ReadFromChannel(x)
      ));
    }
  }

  /**
   * Communicate to the ADC chip via SPI to get single-ended conversion value for a specified channel.
   *
   * @param differential, use the differential between the 2 pins
   * @param channel       analog input channel on ADC chip
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

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  private class ReadFromChannel implements ReadingSupplier<Integer> {

    private final short channel;

    public ReadFromChannel(short channel) {
      this.channel = channel;
    }

    @Override
    public Integer get() throws IOException {
      return readFromChannel(false, channel);
    }
  }

}
