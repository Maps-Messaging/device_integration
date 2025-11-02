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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.model.XRegistrySchemaVersion;

import java.io.IOException;

public class Quad7SegmentController extends HT16K33Controller {

  private final int[] i2cAddr = {0x72};

  public Quad7SegmentController() {
  }

  @Override
  public String getName() {
    return "HT16K33 - 7 Segment";
  }

  @Override
  public String getDescription() {
    return "Quad 7-Segment LED";
  }

  public Quad7SegmentController(AddressableDevice device) throws IOException {
    super(new Quad7Segment(device), device);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Quad7SegmentController(device);
  }

  @Override
  public XRegistrySchemaVersion getSchema() {
    XRegistrySchemaVersion config = super.getSchema(buildSchema(getName(), getDescription()));
    config.setDescription("I2C HT16K33 device drives 4 7 segment LEDs with a : in the center");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}