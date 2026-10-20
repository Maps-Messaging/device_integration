package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class Sen6xCapabilitiesTest {
  @Test
  void sen63cExposesParticlesHumidityTemperatureAndCo2() {
    Sen6xSensor sensor = new Sen63cSensor(bus("SEN63C"));
    Set<String> names = names(sensor);
    assertTrue(names.containsAll(Set.of("CO₂", "temperature", "humidity", "pm_1_0", "pm_2_5", "pm_4_0", "pm_10_0")), names.toString());
    assertFalse(names.contains("vocIndex"));
  }

  @Test
  void sen65ExposesVocNoxAndParticlesWithoutCo2() {
    Set<String> names = names(new Sen65Sensor(bus("SEN65")));
    assertTrue(names.containsAll(Set.of("vocIndex", "noxIndex", "pm_1_0", "pm_2_5", "pm_4_0", "pm_10_0")), names.toString());
    assertFalse(names.contains("CO₂"));
  }

  @Test
  void reservedStatusBitDoesNotIndicateCompensation() {
    assertFalse(new io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data.Sen6xStatus(1 << 15).isCompensationActive());
  }

  private static Set<String> names(Sen6xSensor sensor) {
    return sensor.getReadings().stream().map(r -> r.getName()).collect(Collectors.toSet());
  }

  static ScriptedI2CDevice bus(String model) {
    byte[] text = new byte[32];
    byte[] modelBytes = model.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(modelBytes, 0, text, 0, modelBytes.length);
    int[] words = new int[16];
    for (int index = 0; index < words.length; index++) {
      words[index] = ((text[index * 2] & 0xff) << 8) | (text[index * 2 + 1] & 0xff);
    }
    return new ScriptedI2CDevice(0x6b, frame(words));
  }
}
