/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.i2c.devices.sensors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen65Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xSensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Sen6xConfigContractTest {

  @Test
  void advertisesImplementedControlActionsButNotUnsupportedCleaningInterval() throws IOException {
    Sen6xSensor sensor = new Sen65Sensor(Sen6xCapabilitiesTest.bus("SEN65"));
    TestController controller = new TestController(sensor);
    JsonObject contract = json(controller.getConfigEventDescription());
    JsonObject commands = contract.getAsJsonObject("commands");

    for (String name : new String[]{"START_MEASUREMENT", "STOP_MEASUREMENT", "RESET",
        "START_FAN_CLEANING", "GET_FIRMWARE_VERSION", "CLEAR_DEVICE_STATE"}) {
      assertTrue(commands.has(name), name);
      assertTrue(commands.getAsJsonObject(name).has("parameters"), name);
    }
    assertFalse(commands.has("SET_FAN_CLEANING_INTERVAL"));
    assertFalse(commands.has("GET_FAN_CLEANING_INTERVAL"));
    assertFalse(contract.get("readOnly").getAsBoolean());
  }

  @Test
  void rejectsNonexistentActionsWithoutTouchingHardware() {
    Sen6xSensor sensor = new Sen65Sensor(Sen6xCapabilitiesTest.bus("SEN65"));
    TestController controller = new TestController(sensor);
    assertThrows(IOException.class, () -> controller.executeConfigCommand(
        "{\"command\":\"SET_FAN_CLEANING_INTERVAL\",\"days\":1}".getBytes(StandardCharsets.UTF_8)));
  }

  private static JsonObject json(byte[] bytes) {
    return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static final class TestController extends I2CDeviceController {
    private final Sen6xSensor sensor;

    private TestController(Sen6xSensor sensor) {
      this.sensor = sensor;
    }

    @Override
    public String getName() {
      return "SEN6x";
    }

    @Override
    public String getDescription() {
      return "SEN6x test controller";
    }

    @Override
    public DeviceType getType() {
      return DeviceType.SENSOR;
    }

    @Override
    public I2CDevice getDevice() {
      return sensor;
    }

    @Override
    public SchemaConfig getSchema() {
      return new JsonSchemaConfig("{\"type\":\"object\",\"properties\":{}}");
    }

    @Override
    public I2CDeviceController mount(AddressableDevice device) {
      return this;
    }

    @Override
    public int[] getAddressRange() {
      return new int[]{0x6b};
    }

    @Override
    public boolean detect(AddressableDevice device) {
      return false;
    }
  }
}
