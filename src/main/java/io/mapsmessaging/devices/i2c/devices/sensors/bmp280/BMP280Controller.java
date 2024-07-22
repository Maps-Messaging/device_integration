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

package io.mapsmessaging.devices.i2c.devices.sensors.bmp280;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;

public class BMP280Controller extends I2CDeviceController {

  private final int i2cAddr = 0x78;
  private final BMP280Sensor sensor;

  @Getter
  private final String name = "BMP280";
  @Getter
  private final String description = "Pressure and Temperature Module";

  public BMP280Controller() {
    sensor = null;
  }

  protected BMP280Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new BMP280Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      return new BMP280Controller(device);
    }
  }

  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }
  public DeviceType getType(){
    return getDevice().getType();
  }


  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("pressure", sensor.getPressure());
    jsonObject.put("temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device BMP280 Pressure and Temperature Sensor https://www.bosch-sensortec.com/products/environmental-sensors/pressure-sensors/bmp280/");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("temperature",
            NumberSchema.builder()
                .minimum(-40.0)
                .maximum(80.0)
                .description("Temperature")
                .build()
        )
        .addPropertySchema("pressure",
            NumberSchema.builder()
                .minimum(0.0)
                .maximum(100.0)
                .description("Humidity")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .description("pressure and Temperature Module")
        .title("BMP280");

    return schemaToString(schemaBuilder.build());
  }
}
