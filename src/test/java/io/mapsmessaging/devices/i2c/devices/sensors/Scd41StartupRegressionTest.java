package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.scd41.Scd41Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.DataReadyRequest;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.Scd41Controller;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;

class Scd41StartupRegressionTest {
  @Test
  void scdConfiguresIdleBeforeStartingAndGatesFirstSample() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(0), frame(1), frame(800, 32768, 32768));
    Scd41Sensor sensor = new Scd41Sensor(bus);
    List<String> commands = bus.writes().stream().map(bytes -> HexFormat.of().formatHex(bytes).substring(0, 4)).toList();
    assertTrue(commands.indexOf("3f86") >= 0);
    assertTrue(commands.indexOf("2416") < commands.indexOf("21b1"));
    assertThrows(IllegalStateException.class, () -> sensor.getReadMeasurementRegister().getCo2());
    assertEquals(800, sensor.getReadMeasurementRegister().getCo2());
    assertEquals(800, sensor.getReadMeasurementRegister().getCo2());
  }

  @Test
  void scdReadyStatusUsesLowerElevenBitsAndValidatesCrc() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(0x8000), frame(1));
    DataReadyRequest request = new DataReadyRequest(bus);
    request.getResponse();
    assertFalse(request.isDataReady());
    request.getResponse();
    assertTrue(request.isDataReady());
  }

  @Test
  void scdRejectsBadReadyCrcAndMeasurementCrcWithoutCachingThem() {
    byte[] badReady = frame(1);
    badReady[2] ^= 1;
    DataReadyRequest request = new DataReadyRequest(new ScriptedI2CDevice(0x62, badReady));
    assertThrows(IllegalStateException.class, request::getResponse);
    byte[] badMeasurement = frame(800, 32768, 32768);
    badMeasurement[5] ^= 1;
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(1), badMeasurement, frame(1), frame(900, 32768, 32768));
    Scd41Sensor sensor = new Scd41Sensor(bus);
    assertThrows(IllegalStateException.class, () -> sensor.getReadMeasurementRegister().getCo2());
    assertEquals(900, sensor.getReadMeasurementRegister().getCo2());
  }

  @Test
  void scdPowerCycleDropsTheCachedMeasurement() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(1), frame(800, 32768, 32768), frame(0));
    Scd41Sensor sensor = new Scd41Sensor(bus);
    assertEquals(800, sensor.getReadMeasurementRegister().getCo2());
    sensor.powerOff();
    assertDoesNotThrow(sensor::powerOn);
    assertThrows(IllegalStateException.class, () -> sensor.getReadMeasurementRegister().getCo2());
  }

  @Test
  void failedScdRefreshKeepsThePreviousValidSample() throws Exception {
    byte[] badMeasurement = frame(900, 32768, 32768);
    badMeasurement[5] ^= 1;
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(1), frame(800, 32768, 32768), frame(1), badMeasurement, frame(0));
    Scd41Sensor sensor = new Scd41Sensor(bus);
    assertEquals(800, sensor.getReadMeasurementRegister().getCo2());
    Field lastRead = sensor.getReadMeasurementRegister().getClass().getDeclaredField("lastRead");
    lastRead.setAccessible(true);
    lastRead.setLong(sensor.getReadMeasurementRegister(), 0);
    assertThrows(IllegalStateException.class, () -> sensor.getReadMeasurementRegister().getCo2());
    assertEquals(800, sensor.getReadMeasurementRegister().getCo2());
  }

  @Test
  void scdControllerReportsWarmupAsReadingErrors() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x62, frame(0), frame(0), frame(0));
    Scd41Controller controller =
        new Scd41Controller(bus);
    String json = new String(controller.getDeviceState(), StandardCharsets.UTF_8);
    com.google.gson.JsonObject state = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
    assertEquals("SCD41 measurement unavailable", state.get("CO₂").getAsString());
    assertEquals("SCD41 measurement unavailable", state.get("humidity").getAsString());
    assertFalse(json.equals("{}"), json);
  }


}
