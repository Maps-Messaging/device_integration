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

package io.mapsmessaging.devices.serial.devices.sensors.sen0642;


import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sen0642Controller extends DeviceController {

  private final Sen0642Sensor sensor;

  public Sen0642Controller() {
    sensor = null;
  }

  public Sen0642Controller(InputStream in, OutputStream out) {
    this.sensor = new Sen0642Sensor(in, out);
  }

  @Override
  public String getName() {
    return "SEN0642";

  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0642 UV sensor (UART/Modbus)";
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig cfg = new JsonSchemaConfig(buildSchema(sensor));
    cfg.setTitle(getName());
    cfg.setComments(getDescription());
    cfg.setResourceType("sensor");
    cfg.setVersion(1);
    cfg.setUniqueId(getSchemaId());
    cfg.setDescription("Serial 4800 8N1");
    return cfg;
  }

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    // Defer to base helper if you prefer; here we force a refresh each call.
    sensor.getUvMilliWPerCm2();
    sensor.getUvi();
    String json = buildSchema(sensor, new JsonObject());
    return json.getBytes();
  }

  @Override
  public void setRaiseExceptionOnError(boolean flag) { /* no-op for serial */ }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) {
    return "{}".getBytes();
  }
}