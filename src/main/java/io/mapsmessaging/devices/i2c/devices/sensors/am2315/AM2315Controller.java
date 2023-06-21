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

package io.mapsmessaging.devices.i2c.devices.sensors.am2315;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.schemas.config.impl.meta.Access;
import io.mapsmessaging.schemas.config.impl.meta.Item;
import io.mapsmessaging.schemas.config.impl.meta.JsonSchema;
import io.mapsmessaging.schemas.config.impl.meta.Type;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AM2315Controller implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AM2315Sensor sensor;
  @Getter
  private final String name = "AM2315";

  public AM2315Controller() {
    sensor = null;
  }

  protected AM2315Controller(I2C device) throws IOException {
    sensor = new AM2315Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2315Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("model", sensor.getModel());
    jsonObject.put("status", sensor.getStatus());
    jsonObject.put("version", sensor.getVersion());
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("temperature", sensor.getTemperature());
    jsonObject.put("humidity", sensor.getHumidity());
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    JsonSchema schema = config.getMetaMap();

    Item temperature = new Item("temperature", Type.NUMBER, "Temperature in degrees celcius", Access.READABLE, null);
    Item humidity = new Item("humidity", Type.NUMBER, "Humidity in %", Access.READABLE, null);
    Map<String, Item> entries = new LinkedHashMap<>();
    entries.put(temperature.getName(), temperature);
    entries.put(humidity.getName(), humidity);
    Item updatePayload = new Item("updatePayload", Type.OBJECT, "JSON update payload", Access.READABLE, entries);
    schema.getEntries().put("updatePayload", updatePayload);

    Item model = new Item("model", Type.NUMBER, "Model number of the AM2135", Access.READABLE, null);
    Item status = new Item("status", Type.NUMBER, "Current status of the AM2135", Access.READABLE, null);
    Item version = new Item("version", Type.NUMBER, "Version of the AM2135", Access.READABLE, null);
    Map<String, Item> details = new LinkedHashMap<>();
    details.put(model.getName(), model);
    details.put(status.getName(), status);
    details.put(version.getName(), version);
    Item staticPayload = new Item("staticPayload", Type.OBJECT, "JSON update payload", Access.READABLE, details);
    schema.getEntries().put("staticPayload", staticPayload);

    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    try {
      System.err.println(config.pack());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  public static void main(String[] args){
    AM2315Controller controller = new AM2315Controller();
    controller.getSchema();
  }
}
