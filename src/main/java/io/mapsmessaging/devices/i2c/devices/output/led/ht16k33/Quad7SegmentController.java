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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import lombok.Getter;

public class Quad7SegmentController extends HT16K33Controller {

  private final int[] i2cAddr = {0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77};

  @Getter
  private final String name = "Quad 7-Segment LED";

  public Quad7SegmentController() {
  }

  public Quad7SegmentController(I2C device) {
    super(new Quad7Segment(device));
  }

  public I2CDeviceEntry mount(I2C device) {
    return new Quad7SegmentController(device);
  }

  @Override
  public SchemaConfig getSchema() {
    SchemaConfig config = super.getSchema();
    config.setComments("I2C HT16K33 device drives 4 7 segment LEDs with a : in the center");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}