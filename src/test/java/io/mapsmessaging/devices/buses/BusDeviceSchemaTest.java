package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Controller;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xController;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xDevice;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BusDeviceSchemaTest {

  @TempDir Path directory;

  @Test
  void oneWireSchemaMatchesTemperatureOnlyState() throws Exception {
    Path sample = directory.resolve("w1_slave");
    Files.writeString(sample, "aa bb : crc=cc YES\naa bb t=-1250\n");
    DS18B20Controller controller = new DS18B20Controller(sample.toFile());
    JsonObject schema = ((JsonSchemaConfig) controller.getSchema()).getSchema();
    JsonObject fields = schema.getAsJsonObject("properties");
    JsonObject state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();

    assertEquals(fields.keySet(), state.keySet());
    assertTrue(state.get("temperature").getAsJsonPrimitive().isNumber());
    assertEquals(-1.25f, state.get("temperature").getAsFloat(), 0.0001f);
    assertFalse(schema.get("additionalProperties").getAsBoolean());
    assertEquals(-55, fields.getAsJsonObject("temperature").get("minimum").getAsInt());
    assertEquals(125, fields.getAsJsonObject("temperature").get("maximum").getAsInt());
  }

  @Test
  void spiSchemaMatchesCurrentArrayAndSeparatesConfiguration() {
    for (int resolution : new int[] {10, 12}) {
      for (int channelCount : new int[] {4, 8}) {
        Mcp3y0xController controller = new Mcp3y0xController(
            new Mcp3y0xDevice(spi(new byte[] {0, 0x03, (byte) 0xff}), resolution, channelCount));
        JsonObject schema = ((JsonSchemaConfig) controller.getSchema()).getSchema();
        JsonObject fields = schema.getAsJsonObject("properties");
        JsonObject state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();
        JsonArray readings = state.getAsJsonArray("current");

        assertEquals(fields.keySet(), state.keySet());
        assertEquals(channelCount, readings.size());
        assertTrue(schema.getAsJsonArray("required").contains(new com.google.gson.JsonPrimitive("current")));
        assertEquals(channelCount, fields.getAsJsonObject("current").get("minItems").getAsInt());
        assertEquals(channelCount, fields.getAsJsonObject("current").get("maxItems").getAsInt());
        assertEquals((1 << resolution) - 1,
            fields.getAsJsonObject("current").getAsJsonObject("items").get("maximum").getAsInt());
        assertTrue(schema.getAsJsonObject("x-deviceConfiguration").getAsJsonObject("properties").has("resolution"));
        assertFalse(fields.has("resolution"));
      }
    }
  }

  @Test
  void unmountedSpiControllerDeclaresEmptyCurrentArray() {
    Mcp3y0xController controller = new Mcp3y0xController();
    JsonObject schema = ((JsonSchemaConfig) controller.getSchema()).getSchema();
    JsonObject state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();
    assertEquals(0, state.getAsJsonArray("current").size());
    assertEquals(0, schema.getAsJsonObject("properties").getAsJsonObject("current").get("maxItems").getAsInt());
  }

  private static Spi spi(byte[] reply) {
    return (Spi) Proxy.newProxyInstance(Spi.class.getClassLoader(), new Class<?>[] {Spi.class},
        (proxy, method, arguments) -> {
          if ("transfer".equals(method.getName()) && arguments.length == 2
              && arguments[0] instanceof byte[] && arguments[1] instanceof byte[]) {
            System.arraycopy(reply, 0, (byte[]) arguments[1], 0, reply.length);
            return method.getReturnType() == int.class ? reply.length : null;
          }
          throw new AssertionError("Unexpected SPI call: " + method.getName());
        });
  }
}
