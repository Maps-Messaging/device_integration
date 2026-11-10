/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.i2c;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class I2CConfigurationContractTest {

  @Test
  void configTopicAdvertisesSupportedCommandsWithoutJavaClasses() throws IOException {
    TestController controller = new TestController();
    I2CDeviceScheduler scheduler = new I2CDeviceScheduler(controller);
    JsonObject description = parse(scheduler.getDeviceConfiguration());

    assertEquals("TEST_LCD", description.get("device").getAsString());
    assertTrue(description.getAsJsonObject("commands").has("WRITE"));
    assertTrue(description.getAsJsonObject("commands").has("CLEAR"));
    assertFalse(description.has("className"));
    assertFalse(description.get("readOnly").getAsBoolean());
  }

  @Test
  void namedCommandDispatchesToExistingControllerAction() throws IOException {
    TestController controller = new TestController();
    I2CDeviceScheduler scheduler = new I2CDeviceScheduler(controller);
    JsonObject response = parse(scheduler.updateDeviceConfiguration(
        "{\"command\":\"WRITE\",\"address\":0}".getBytes(StandardCharsets.UTF_8)));

    assertEquals("WRITE", controller.lastCommand.get("action").getAsString());
    assertEquals(0, controller.lastCommand.get("address").getAsInt());
    assertFalse(controller.lastCommand.has("command"));
    assertFalse(response.has("@class"));
    assertEquals("success", response.get("status").getAsString());
  }

  @Test
  void legacyPayloadRemainsUnchanged() throws IOException {
    TestController controller = new TestController();
    I2CDeviceScheduler scheduler = new I2CDeviceScheduler(controller);
    scheduler.updateDeviceConfiguration("{\"action\":\"CLEAR\"}".getBytes(StandardCharsets.UTF_8));
    assertEquals("CLEAR", controller.lastCommand.get("action").getAsString());
  }

  @Test
  void unknownCommandIsRejected() {
    TestController controller = new TestController();
    I2CDeviceScheduler scheduler = new I2CDeviceScheduler(controller);
    assertThrows(IOException.class, () -> scheduler.updateDeviceConfiguration(
        "{\"command\":\"FORMAT\"}".getBytes(StandardCharsets.UTF_8)));
  }

  private static JsonObject parse(byte[] data) {
    return JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static final class TestController extends I2CDeviceController {
    private JsonObject lastCommand;

    @Override
    public String getName() {
      return "TEST_LCD";
    }

    @Override
    public String getDescription() {
      return "Test display";
    }

    @Override
    public SchemaConfig getSchema() {
      return new JsonSchemaConfig("""
          {
            "type": "object",
            "properties": {},
            "x-deviceWrite": {
              "type": "object",
              "properties": {
                "action": {"type":"string","enum":["WRITE","CLEAR"]},
                "address": {"type":"integer","minimum":0}
              },
              "required": ["action"]
            }
          }
          """);
    }

    @Override
    public byte[] getDeviceConfiguration() {
      return "{}".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] updateDeviceConfiguration(byte[] data) {
      lastCommand = parse(data);
      return "{\"@class\":\"Response\",\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getDeviceState() {
      return "{}".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public DeviceType getType() {
      return DeviceType.DISPLAY;
    }

    @Override
    public I2CDevice getDevice() {
      return null;
    }

    @Override
    public I2CDeviceController mount(AddressableDevice device) {
      return this;
    }

    @Override
    public boolean detect(AddressableDevice device) {
      return false;
    }

    @Override
    public int[] getAddressRange() {
      return new int[0];
    }
  }
}
