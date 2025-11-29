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

package io.mapsmessaging.devices.serial.devices.sensors.sen0640;


import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.util.List;

public class Sen0640Controller extends DeviceController {

  private final Sen0640Sensor sensor;

  public Sen0640Controller() {
    sensor = null;
  }

  public Sen0640Controller(SerialDevice serial) throws IOException {
    this.sensor = new Sen0640Sensor(serial);
  }

  @Override
  public String getName() {
    return "SEN0640";

  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0640 Solar Radiation sensor (UART/Modbus)";
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
    try {
      JsonObject jsonObject = new JsonObject();
      List<SensorReading<?>> readings = sensor.getReadings();
      walkSensorReadings(jsonObject, readings);
      return convert(jsonObject);
    } catch (Exception e) {
      // Log this
    }
    return "{}".getBytes();
  }

  @Override
  public void setRaiseExceptionOnError(boolean flag) { /* no-op for serial */ }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) {
    return "{}".getBytes();
  }
}