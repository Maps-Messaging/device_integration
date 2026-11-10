/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Controller;
import io.mapsmessaging.devices.serial.devices.sensors.sen0640.Sen0640Controller;
import io.mapsmessaging.devices.spi.SpiDeviceScheduler;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xController;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DeviceConfigContractTest {

  @Test
  void oneWireDeviceAdvertisesReadOnlyConfigContract() throws IOException {
    assertReadOnly(new DS18B20Controller().getDeviceConfiguration(), "DS18B20");
  }

  @Test
  void serialDeviceAdvertisesReadOnlyConfigContract() throws IOException {
    assertReadOnly(new Sen0640Controller().getDeviceConfiguration(), "SEN0640");
  }

  @Test
  void spiAdcDoesNotAdvertiseUnimplementedSetters() throws IOException {
    assertReadOnly(new SpiDeviceScheduler(new Mcp3y0xController()).getDeviceConfiguration(), "Mcp3y0x");
  }

  private static void assertReadOnly(byte[] bytes, String expectedName) {
    JsonObject contract = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals(expectedName, contract.get("device").getAsString());
    assertEquals(0, contract.getAsJsonObject("commands").size());
    assertTrue(contract.get("readOnly").getAsBoolean());
  }
}
