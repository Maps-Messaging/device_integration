package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.*;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.*;
import org.junit.jupiter.api.Test;

class Sen6xMeasurementTest {

  @Test
  void sen63cReportsUnscaledSignedCo2Ppm() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1),
        frame(100, 250, 400, 1000, 5000, -1000, 900));
    var block = new Sen63cMeasurementManager(helper(bus)).getMeasurementBlock();
    assertEquals(900.0f, block.getCo2ppm());
    assertEquals(-5.0f, block.getTemperatureC());
    assertEquals(50.0f, block.getHumidityPercent());
    assertEquals(25.0f, block.getPm2_5());
  }

  @Test
  void sen65DecodesAllEightMeasurements() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1),
        frame(100, 250, 400, 1000, 5000, -1000, 1234, 567));
    var block = new Sen65MeasurementManager(helper(bus)).getMeasurementBlock();
    assertEquals(123.4f, block.getVocIndex(), 0.01f);
    assertEquals(56.7f, block.getNoxIndex(), 0.01f);
    assertEquals(100.0f, block.getPm10_0());
    assertEquals(-5.0f, block.getTemperatureC());
  }

  @Test
  void sen66Co2IsUnsignedPpm() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1),
        frame(100, 250, 400, 1000, 5000, -1000, 1234, 567, 40000));
    assertEquals(40000.0f, new Sen66MeasurementManager(helper(bus)).getMeasurementBlock().getCo2ppm());
  }

  @Test
  void sen68FormaldehydeIsScaledInPpb() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1),
        frame(100, 250, 400, 1000, 5000, -1000, 1234, 567, 125));
    assertEquals(12.5f, new Sen68MeasurementManager(helper(bus)).getMeasurementBlock().getHchoPpb());
  }

  @Test
  void unknownWordsAreNotReportedAsMeasurements() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1),
        frame(0xffff, 0xffff, 0xffff, 0xffff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0xffff));
    var block = new Sen66MeasurementManager(helper(bus)).getMeasurementBlock();
    assertTrue(Float.isNaN(block.getPm1_0()));
    assertTrue(Float.isNaN(block.getTemperatureC()));
    assertTrue(Float.isNaN(block.getVocIndex()));
    assertTrue(Float.isNaN(block.getCo2ppm()));
  }

  @Test
  void newManagerDoesNotImmediatelyTriggerRecovery() {
    assertFalse(new Sen63cMeasurementManager(helper(new ScriptedI2CDevice(0x6b))).hasLocked());
  }

  @Test
  void readyFlagReadFailureDoesNotReturnHealthyCachedZeros() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, new byte[] {0, 1, 0});
    assertThrows(java.io.IOException.class,
        () -> new Sen66MeasurementManager(helper(bus)).getMeasurementBlock());
  }
}
