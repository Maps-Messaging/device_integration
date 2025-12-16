/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.pn532;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Pn532Controller extends I2CDeviceController {

  private final Pn532Sensor sensor;

  public Pn532Controller(){
    sensor = null;
  }

  protected Pn532Controller(AddressableDevice device) {
    this.sensor = new Pn532Sensor(device);
  }

  @Override
  public String getName() {
    return "PN532";
  }

  @Override
  public String getDescription() {
    return "PN532 NFC Reader (I2C)";
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig cfg = new JsonSchemaConfig(buildSchema(sensor));
    cfg.setTitle(getName());
    cfg.setComments(getDescription());
    cfg.setResourceType("sensor");
    cfg.setVersion(1);
    cfg.setUniqueId(getSchemaId());
    cfg.setDescription("I2C address 0x24");
    return cfg;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{Pn532Sensor.I2C_ADDR};
  }

  @Override
  public boolean detect(AddressableDevice dev) {
    // Lightweight address match; you can upgrade to a real probe by sending SAMConfiguration and catching IOE.
    return dev.getDevice() == Pn532Sensor.I2C_ADDR;
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Pn532Controller(device);
  }

  @Override
  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }
}