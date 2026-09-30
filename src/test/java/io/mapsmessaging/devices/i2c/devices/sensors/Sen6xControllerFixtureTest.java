package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.*;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

class Sen6xControllerFixtureTest {
  @TestFactory
  Stream<DynamicTest> supportedModelsDecodeThroughControllerJson() {
    return Stream.of("SEN63C", "SEN65", "SEN66", "SEN68").map(model -> DynamicTest.dynamicTest(model, () -> {
      ModelDevice bus = new ModelDevice(model);
      var controller = new Sen6xController().mount(bus);
      try {
        JsonObject state = JsonParser.parseString(new String(controller.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(model, state.get("Product Name").getAsString());
        assertEquals("unit-123", state.get("Serial Number").getAsString());
        assertEquals(10f, state.get("pm_1_0").getAsFloat());
        assertEquals(25f, state.get("pm_2_5").getAsFloat());
        assertEquals(40f, state.get("pm_4_0").getAsFloat());
        assertEquals(100f, state.get("pm_10_0").getAsFloat());
        assertEquals(50f, state.get("humidity").getAsFloat());
        assertEquals(20f, state.get("temperature").getAsFloat());
        assertEquals(9.3f, state.get("dewPoint").getAsFloat(), 0.1f);
        if (model.equals("SEN63C") || model.equals("SEN66")) assertEquals(900f, state.get("CO₂").getAsFloat());
        else assertFalse(state.has("CO₂"));
        if (!model.equals("SEN63C")) {
          assertEquals(123f, state.get("vocIndex").getAsFloat());
          assertEquals(57f, state.get("noxIndex").getAsFloat());
        }
        if (model.equals("SEN68")) assertEquals(12.5f, state.get("hcho").getAsFloat());
        JsonObject status = state.getAsJsonObject("status");
        for (String flag : List.of("Fan Error", "RHT Error", "Gas Error", "CO₂-2 Error", "HCHO Error", "PM Error", "CO₂-1 Error", "Speed Warning")) {
          assertTrue(status.get(flag).getAsBoolean(), flag);
        }
        assertFalse(status.has("Compensation Active"));
        assertEquals(List.of(0x0104, 0xd304, 0x0021), bus.commands.subList(2, 5));
        assertNotNull(controller.getSchema());
      } finally { controller.close(); }
      assertEquals(1, bus.closes);
    }));
  }

  @Test
  void unknownModelIsRejectedBeforeStartingMeasurement() {
    ModelDevice bus = new ModelDevice("OTHER");
    assertThrows(IOException.class, () -> new Sen6xController().mount(bus));
    assertFalse(new Sen6xController().detect(bus));
    assertFalse(bus.commands.contains(0x0021));
  }

  @Test
  void warmupCo2IsAnErrorResultRatherThanZero() throws Exception {
    ModelDevice bus = new ModelDevice("SEN66");
    bus.unknownCo2 = true;
    var controller = new Sen6xController().mount(bus);
    try {
      var sensor = (Sen6xSensor) controller.getDevice();
      var result = sensor.getReadings().stream().filter(r -> r.getName().equals("CO₂")).findFirst().orElseThrow().getValue();
      assertTrue(result.hasError());
      assertInstanceOf(IOException.class, result.getError());
      assertTrue(result.getError().getMessage().contains("unavailable"));
    } finally { controller.close(); }
  }

  @Test
  void notReadyDoesNotReadMeasurementAndInvalidationDiscardsCachedSample() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(0), frame(1),
        frame(100, 250, 400, 1000, 5000, 4000, 1234, 567, 900), frame(0));
    var manager = new Sen66MeasurementManager(Sen6xProtocolTest.helper(bus));
    assertTrue(Float.isNaN(manager.getMeasurementBlock().getCo2ppm()));
    assertEquals(900f, manager.getMeasurementBlock().getCo2ppm());
    manager.invalidate();
    assertTrue(Float.isNaN(manager.getMeasurementBlock().getCo2ppm()));
    assertEquals(1, bus.writes().stream().filter(b -> Arrays.equals(b, new byte[] {3, 0})).count());
  }

  @Test
  void asciiStopsAtFirstNullAndClearStatusReadsBothWords() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(0x4120, 0, 0x4243), frame(0, 0), frame(0, 0));
    var helper = Sen6xProtocolTest.helper(bus);
    assertEquals("A", helper.requestAsciiResponse(0xd014, 9));
    assertTrue(new ClearDeviceStatusCommand(helper).execute());
    assertNull(new ClearDeviceStateCommand(helper).execute());
    assertArrayEquals(new byte[] {(byte) 0xd2, 0x10}, bus.writes().get(1));
    assertArrayEquals(new byte[] {(byte) 0xd2, 0x10}, bus.writes().get(2));
  }

  @Test
  void manualCleaningStopsMeasurementAndResumesAfterCleaning() throws Exception {
    ModelDevice bus = new ModelDevice("SEN66");
    var controller = new Sen6xController().mount(bus);
    try {
      var sensor = (Sen6xSensor) controller.getDevice();
      bus.commands.clear();
      sensor.startFanCleaning();
      assertTrue(sensor.isMeasuring());
      assertEquals(List.of(0x0104, 0x5607, 0x0021), bus.commands);
    } finally { controller.close(); }
  }

  @Test
  void publicVersionAndClearStatusUseSpecifiedCommands() throws Exception {
    ModelDevice bus = new ModelDevice("SEN66");
    var controller = new Sen6xController().mount(bus);
    try {
      var sensor = (Sen6xSensor) controller.getDevice();
      bus.commands.clear();
      assertEquals("1.2", sensor.getFirmwareVersion());
      sensor.clearDeviceState();
      assertEquals(List.of(0xd100, 0xd210), bus.commands);
      assertThrows(UnsupportedOperationException.class, sensor::getFanCleaningInterval);
      assertThrows(UnsupportedOperationException.class, () -> sensor.setFanCleaningInterval(7));
      assertEquals(2, bus.commands.size());
    } finally { controller.close(); }
  }

  static class ModelDevice extends ScriptedI2CDevice {
    private final String model;
    private final List<Integer> commands = new ArrayList<>();
    private int current;
    private int closes;
    private boolean unknownCo2;
    ModelDevice(String model) { super(0x6b); this.model = model; }
    @Override public int write(byte[] bytes, int offset, int length) {
      assertEquals(2, length, "Each modeled command has no payload");
      current = ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
      commands.add(current);
      return length;
    }
    @Override public int read(byte[] buffer, int offset, int length) {
      byte[] response = switch (current) {
        case 0xd014 -> ascii(model);
        case 0xd033 -> ascii("unit-123");
        case 0xd206 -> frame(0x20, 0x1ed0);
        case 0xd210 -> frame(0, 0);
        case 0xd100 -> frame(0x0102);
        case 0x0202 -> frame(1);
        case 0x0471 -> frame(100, 250, 400, 1000, 5000, 4000, 900);
        case 0x0446 -> frame(100, 250, 400, 1000, 5000, 4000, 1234, 567);
        case 0x0300 -> frame(100, 250, 400, 1000, 5000, 4000, 1234, 567, unknownCo2 ? 0xffff : 900);
        case 0x0467 -> frame(100, 250, 400, 1000, 5000, 4000, 1234, 567, 125);
        default -> throw new AssertionError("Unexpected read command " + Integer.toHexString(current));
      };
      assertEquals(response.length, length, "Driver requested wrong wire length");
      System.arraycopy(response, 0, buffer, offset, length);
      return length;
    }
    @Override public void close() { closes++; }
    private static byte[] ascii(String value) {
      byte[] text = Arrays.copyOf(value.getBytes(StandardCharsets.US_ASCII), 32);
      int[] words = new int[16];
      for (int i = 0; i < words.length; i++) words[i] = ((text[2 * i] & 0xff) << 8) | (text[2 * i + 1] & 0xff);
      return frame(words);
    }
  }
}
