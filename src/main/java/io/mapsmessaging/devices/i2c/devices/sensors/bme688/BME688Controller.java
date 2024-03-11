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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;

import java.io.IOException;

public class BME688Controller extends I2CDeviceController {

  private final int i2cAddr = 0x77;
  private final BME688Sensor sensor;

  @Getter
  private final String name = "BME688";
  @Getter
  private final String description = "VOC, Humidity, Pressure and Temperature Module";

  public BME688Controller() {
    sensor = null;
  }

  protected BME688Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new BME688Sensor(device);
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
      return new BME688Controller(device);
    }
  }
  public DeviceType getType(){
    return getDevice().getType();
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device BME688 VOC, Pressure, Temperature and humidity Sensor");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
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
        .description("VOC, Humidity, pressure and Temperature Module")
        .title("BME688");

    return schemaToString(schemaBuilder.build());
  }
}
