package io.mapsmessaging.devices;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Controller;
import io.mapsmessaging.devices.sensorreadings.GroupSensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupedSensorReadingTest {
  private GroupSensorReading group() {
    GroupSensorReading group = new GroupSensorReading("status", "", "Status", null, true, null);
    group.getGroupList().add(new StringSensorReading("child", "", "Child", "ok", true, () -> "ok"));
    return group;
  }

  @Test
  void groupedValuesAreSerialized() throws IOException {
    JsonObject value = new ExposedController().read(group());
    assertTrue(value.has("status"), value.toString());
    assertEquals("ok", value.getAsJsonObject("status").get("child").getAsString());
  }

  @Test
  void groupedSchemaHasPropertiesAndOwnRequiredFields() {
    JsonObject schema = JsonParser.parseString(SchemaBuilder.buildSchema(() -> List.of(group()), null)).getAsJsonObject();
    JsonObject status = schema.getAsJsonObject("properties").getAsJsonObject("status");
    assertTrue(status.has("type"), status.toString());
    assertEquals("object", status.get("type").getAsString());
    assertEquals("string", status.getAsJsonObject("properties").getAsJsonObject("child").get("type").getAsString());
    assertEquals("child", status.getAsJsonArray("required").get(0).getAsString());
    assertEquals("status", schema.getAsJsonArray("required").get(0).getAsString());
  }

  private static class ExposedController extends Bh1750Controller {
    JsonObject read(GroupSensorReading group) throws IOException {
      JsonObject root = new JsonObject();
      walkSensorReadings(root, List.of(group));
      return root;
    }
  }
}
