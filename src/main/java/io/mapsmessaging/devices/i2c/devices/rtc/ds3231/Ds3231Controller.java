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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

public class Ds3231Controller extends I2CDeviceController {

  private final int i2cAddr = 0x68;
  private final Ds3231Rtc rtc;
  private final JsonPacker packer;
  private final JsonUnpacker unpacker;

  @Getter
  private final String name = "DS3231";


  public Ds3231Controller() {
    rtc = null;
    packer = null;
    unpacker = null;
  }

  public Ds3231Controller(I2C device) {
    rtc = new Ds3231Rtc(device);
    packer = new JsonPacker(rtc);
    unpacker = new JsonUnpacker(rtc);
  }

  @Override
  public boolean detect() {
    return rtc != null && rtc.isConnected();
  }

  public I2CDeviceController mount(I2C device) {
    return new Ds3231Controller(device);
  }

  @Override
  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (rtc != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] getUpdatePayload() {
    if (packer != null && rtc != null) {
      rtc.read();
      return packer.pack();
    }
    return "{}".getBytes();
  }

  @Override
  public void setPayload(byte[] val) {
    if (unpacker != null) {
      JSONObject jsonObject = new JSONObject(new String(val));
      unpacker.unpack(jsonObject);
    }
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c RTC");
    config.setSource("I2C bus address : 0x68");
    config.setVersion("1.0");
    config.setResourceType("rtc");
    config.setInterfaceDescription("Returns JSON object containing the latest rtc");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, SchemaHelper.generateUpdatePayloadSchema())
        .addPropertySchema(NamingConstants.DEVICE_WRITE_SCHEMA, SchemaHelper.buildWritablePayload())
        .description("Quad 7 Segment LED")
        .title("HT16K33");

    return schemaToString(schemaBuilder.build());
  }
}