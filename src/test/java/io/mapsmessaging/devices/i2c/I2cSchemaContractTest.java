package io.mapsmessaging.devices.i2c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Controller;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Controller;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.Lcd1602Controller;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.backlight.BacklightV1PwmController;
import io.mapsmessaging.devices.i2c.devices.output.lcd.st7735.St7735Controller;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.Quad7SegmentController;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.BNO055Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.Ina219Controller;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.AT24CnnController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class I2cSchemaContractTest {

  @Test
  void sensorSchemaPermitsErrorTextAndUnavailableReadings() throws Exception {
    StubController controller = new StubController();
    JsonObject schema = controller.getSchema().getSchema();
    assertFalse(schema.has("required"), "An I2C sensor can return {} after a failed poll");
    JsonObject reading = schema.getAsJsonObject("properties").getAsJsonObject("temperature");
    assertEquals("number", reading.getAsJsonArray("anyOf").get(0).getAsJsonObject().get("type").getAsString());
    assertEquals("string", reading.getAsJsonArray("anyOf").get(1).getAsJsonObject().get("type").getAsString());
    JsonObject state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();
    assertTrue(state.get("temperature").isJsonPrimitive());
    assertTrue(state.get("temperature").getAsJsonPrimitive().isString());
    assertTrue(schema.getAsJsonObject("properties").has("timestamp"));
  }

  @Test
  void ina219ConfigurationIsNotFalselyDeclaredAsReading() {
    Ina219Controller controller = new Ina219Controller();
    JsonObject schema = ((JsonSchemaConfig) controller.getSchema()).getSchema();
    assertFalse(schema.getAsJsonObject("properties").has("adcResolution"));
    assertTrue(schema.getAsJsonObject("properties").has("bus_voltage"));
    assertTrue(schema.getAsJsonObject("x-deviceConfiguration").getAsJsonObject("properties").has("adcResolution"));
  }

  @Test
  void bno055FlatStateSchemaMatchesController() {
    JsonObject schema = ((JsonSchemaConfig) new BNO055Controller().getSchema()).getSchema();
    JsonObject props = schema.getAsJsonObject("properties");
    assertTrue(props.has("heading"));
    assertTrue(props.has("roll"));
    assertTrue(props.has("pitch"));
    assertFalse(props.has("gravity"));
    JsonObject config = schema.getAsJsonObject("x-deviceConfiguration").getAsJsonObject("properties");
    assertTrue(config.has("calibrationStatus"));
    assertTrue(config.has("version"));
  }

  @Test
  void eepromStateAndBinaryCommandsAreSeparate() {
    JsonObject schema = ((JsonSchemaConfig) new AT24CnnController().getSchema()).getSchema();
    assertTrue(schema.getAsJsonObject("properties").has("size"));
    JsonObject command = schema.getAsJsonObject("x-deviceCommand").getAsJsonObject("properties");
    assertTrue(command.has("action"));
    assertTrue(command.has("address"));
    assertTrue(command.has("data"));
    assertTrue(schema.has("x-deviceResponse"));
  }

  @Test
  void displaySchemasDescribeOnlyImplementedOperationsAndState() {
    JsonObject lcd = ((JsonSchemaConfig) new Lcd1602Controller().getSchema()).getSchema();
    JsonObject lcdProps = lcd.getAsJsonObject("properties");
    assertFalse(lcdProps.has("deviceStatic"));
    assertTrue(lcdProps.has("deviceWrite"));
    assertTrue(lcdProps.has("deviceState"));
    assertTrue(lcdProps.getAsJsonObject("deviceState").getAsJsonObject("properties").isEmpty());

    JsonObject st7735 = ((JsonSchemaConfig) new St7735Controller().getSchema()).getSchema();
    JsonObject command = st7735.getAsJsonObject("properties").getAsJsonObject("deviceWrite")
        .getAsJsonObject("properties").getAsJsonObject("command");
    assertEquals(3, command.getAsJsonArray("enum").size());

    JsonObject ht16k33 = ((JsonSchemaConfig) new Quad7SegmentController().getSchema()).getSchema();
    JsonObject ledState = ht16k33.getAsJsonObject("properties").getAsJsonObject("deviceState").getAsJsonObject("properties");
    assertTrue(ledState.has("display"));
    assertTrue(ledState.has("brightness"));
    assertTrue(ledState.has("enabled"));
  }

  @Test
  void outputControllersReturningEmptyObjectsHaveEmptyStateSchemas() {
    assertEmptyState(((JsonSchemaConfig) new Pca9685Controller().getSchema()).getSchema());
    assertEmptyState(((JsonSchemaConfig) new Mcp23017Controller().getSchema()).getSchema());
    assertEmptyState(((JsonSchemaConfig) new BacklightV1PwmController().getSchema()).getSchema());
  }

  private static void assertEmptyState(JsonObject schema) {
    assertEquals("object", schema.get("type").getAsString());
    assertTrue(schema.getAsJsonObject("properties").isEmpty());
    assertFalse(schema.get("additionalProperties").getAsBoolean());
  }

  private static final class StubController extends I2CDeviceController {
    private final StubSensor sensor = new StubSensor();

    @Override public String getName() { return "Stub temperature"; }
    @Override public String getDescription() { return "A simulated failing sensor"; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public boolean detect(AddressableDevice bus) { return false; }
    @Override public int[] getAddressRange() { return new int[] {0x42}; }
    @Override public I2CDeviceController mount(AddressableDevice bus) { return this; }
    @Override public I2CDevice getDevice() { return sensor; }
    @Override public JsonSchemaConfig getSchema() { return new JsonSchemaConfig(buildSchema(sensor)); }
  }

  private static final class StubSensor extends I2CDevice implements Sensor {
    private final List<SensorReading<?>> readings;

    StubSensor() {
      super(new RegisterBackedI2cDevice(0x42), LoggerFactory.getLogger(StubSensor.class));
      readings = List.of(new FloatSensorReading(
          "temperature", "°C", "A test reading", 25.0f, true, -40f, 85f, 1,
          () -> { throw new IOException("Simulated sensor read failure"); }));
    }

    @Override public boolean isConnected() { return false; }
    @Override public String getName() { return "Stub temperature"; }
    @Override public String getDescription() { return "A simulated failing sensor"; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public List<SensorReading<?>> getReadings() { return readings; }
  }
}
