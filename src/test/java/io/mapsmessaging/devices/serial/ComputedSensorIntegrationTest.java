package io.mapsmessaging.devices.serial;

import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ComputedSensorIntegrationTest {

  @Test
  void lidarSecondarySharesOnePortAndHasIndependentLifecycle() throws Exception {
    TestPort port = new TestPort();
    SerialBusManager manager = new SerialBusManager();
    SerialDeviceController primary = manager.mount("SEN0547", port);
    assertNotNull(primary);
    assertSame(primary, manager.mount("SEN0547", port));
    assertEquals(1, port.opens.get());
    assertSame(primary, manager.getActive().get("test-port"));
    DeviceController child = manager.getActive().get("test-port_change");
    assertNotNull(child);
    assertNotEquals(primary.getSchemaId(), child.getSchemaId());
    assertTrue(new String(child.getDeviceState(), StandardCharsets.UTF_8).contains("hasData"));
    manager.unmount((SerialDeviceController) child);
    assertFalse(manager.getActive().containsKey("test-port_change"));
    assertTrue(port.open);
    assertThrows(java.io.IOException.class, child::getDeviceState);
    primary.close();
    assertFalse(port.open);
  }

  @Test
  void radarSecondarySharesOnePortAndSupportsOwnThresholdConfiguration() throws Exception {
    TestPort port = new TestPort();
    SerialBusManager manager = new SerialBusManager();
    SerialDeviceController primary = manager.mount("SEN0609", port);
    assertNotNull(primary);
    DeviceController child = manager.getActive().get("test-port_change");
    assertNotNull(child);
    assertEquals("SEN0609_change", child.getName());
    assertNotEquals(primary.getSchemaId(), child.getSchemaId());
    byte[] reply = child.updateDeviceConfiguration(
        "{\"command\":\"SET_DISTANCE_THRESHOLD\",\"metres\":0.3}".getBytes(StandardCharsets.UTF_8));
    assertTrue(new String(reply, StandardCharsets.UTF_8).contains("success"));
    assertEquals(1, port.opens.get());
    manager.unmount((SerialDeviceController) child);
    assertTrue(port.open);
    assertThrows(java.io.IOException.class, child::getDeviceState);
    primary.close();
    assertFalse(port.open);
  }

  private static final class TestPort implements SerialDevice {
    private final AtomicInteger opens = new AtomicInteger();
    private volatile boolean open;

    @Override public boolean isOpen() { return open; }
    @Override public boolean openPort() { opens.incrementAndGet(); open = true; return true; }
    @Override public void closePort() { open = false; }
    @Override public String getSystemPortName() { return "test-port"; }
    @Override public int writeBytes(byte[] bytes, int length) { return length; }
    @Override public int readBytes(byte[] bytes, int length) { return 0; }
  }
}
