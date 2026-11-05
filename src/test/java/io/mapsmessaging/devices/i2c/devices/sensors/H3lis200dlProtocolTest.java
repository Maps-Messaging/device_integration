package io.mapsmessaging.devices.i2c.devices.sensors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.SchemaBuilder;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.h3lis200dl.H3LIS200DLController;
import io.mapsmessaging.devices.i2c.devices.sensors.h3lis200dl.H3LIS200DLSensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class H3lis200dlProtocolTest {
  @Test
  void detectsBothAddressesWithoutWritingAndRegistersProvider() {
    H3LIS200DLController controller = new H3LIS200DLController();
    assertArrayEquals(new int[]{0x18, 0x19}, controller.getAddressRange());
    assertTrue(controller.canDetect());
    for (int address : controller.getAddressRange()) {
      RegisterDevice device = new RegisterDevice(address);
      assertTrue(controller.detect(device));
      assertTrue(device.writes.isEmpty());
      assertEquals(List.of(0x0f), device.reads);
    }
    assertTrue(ServiceLoader.load(I2CDeviceController.class).stream()
        .anyMatch(provider -> provider.type() == H3LIS200DLController.class));
  }

  @Test
  void rejectsOtherIdsAndFailedIdentityReadsWithoutConfiguration() {
    H3LIS200DLController controller = new H3LIS200DLController();
    RegisterDevice device = new RegisterDevice(0x19);
    device.image[0x0f] = 0x33;
    assertFalse(controller.detect(device));
    assertThrows(IOException.class, () -> controller.mount(device));
    assertTrue(device.writes.isEmpty());
    device.failedRegister = 0x0f;
    assertFalse(controller.detect(device));
    assertThrows(IOException.class, () -> controller.mount(device));
  }

  @Test
  void initializesHundredGNormalHundredHzWithAllAxesEnabled() throws Exception {
    RegisterDevice device = new RegisterDevice(0x19);
    new H3LIS200DLSensor(device);
    assertEquals(0x2f, device.image[0x20] & 255);
    assertEquals(0, device.image[0x21] & 255);
    assertEquals(0, device.image[0x23] & 255);
    assertEquals(0, device.image[0x24] & 255);
    assertArrayEquals(new byte[]{0x20, 0x07}, device.writes.getFirst());
    assertArrayEquals(new byte[]{0x20, 0x2f}, device.writes.getLast());
  }

  @Test
  void decodesSignedEightBitAxesUsingDatasheetSensitivityInMetricUnits() throws Exception {
    RegisterDevice device = new RegisterDevice(0x19).axes(0x40, 0xc0, 0xff);
    H3LIS200DLSensor sensor = new H3LIS200DLSensor(device);
    assertEquals(489.54797f, sensor.getX(), 0.0001f);
    assertEquals(-489.54797f, sensor.getY(), 0.0001f);
    assertEquals(-7.649187f, sensor.getZ(), 0.0001f);
    device.axes(0x80, 0x7f, 0);
    assertEquals(-979.09594f, sensor.getX(), 0.0001f);
    assertEquals(971.44675f, sensor.getY(), 0.0001f);
    assertEquals(0, sensor.getZ());
  }

  @Test
  void shortOrFailedReadsFailAndCanBeRetried() throws Exception {
    RegisterDevice device = new RegisterDevice(0x19).axes(1, 2, 3);
    H3LIS200DLSensor sensor = new H3LIS200DLSensor(device);
    device.shortRead = true;
    assertThrows(IOException.class, sensor::getX);
    device.shortRead = false;
    device.failedRegister = 0x29;
    assertThrows(IOException.class, sensor::getX);
    device.failedRegister = -1;
    assertEquals(7.649187f, sensor.getX(), 0.0001f);
  }

  @Test
  void failedConfigurationWritePreventsMounting() {
    RegisterDevice device = new RegisterDevice(0x18);
    device.failWrites = true;
    assertThrows(IOException.class, () -> new H3LIS200DLController().mount(device));
  }

  @Test
  void publishesThreeMetricReadingsAndSchemaThroughNormalController() throws Exception {
    RegisterDevice device = new RegisterDevice(0x18).axes(1, 0xff, 0);
    I2CDeviceController controller = new H3LIS200DLController().mount(device);
    H3LIS200DLSensor sensor = (H3LIS200DLSensor) controller.getDevice();
    JsonObject state = JsonParser.parseString(new String(controller.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals(7.65f, state.get("x_acceleration").getAsFloat(), 0.001f);
    assertEquals(-7.65f, state.get("y_acceleration").getAsFloat(), 0.001f);
    assertEquals(0, state.get("z_acceleration").getAsFloat());
    assertEquals("H3LIS200DL", controller.getName());
    assertNotNull(controller.getSchema());
    JsonObject properties = JsonParser.parseString(SchemaBuilder.buildSchema(sensor, null)).getAsJsonObject().getAsJsonObject("properties");
    assertEquals("number", properties.getAsJsonObject("x_acceleration").get("type").getAsString());
    assertEquals(-980.665f, properties.getAsJsonObject("x_acceleration").get("minimum").getAsFloat(), 0.001f);
    assertTrue(sensor.getReadings().stream().allMatch(reading -> "m/s²".equals(reading.getUnit())));
    assertTrue(sensor.isConnected());
    device.image[0x0f] = 0;
    assertFalse(sensor.isConnected());
    device.failedRegister = 0x0f;
    assertFalse(sensor.isConnected());
  }

  private static final class RegisterDevice implements AddressableDevice {
    private final int address;
    private final byte[] image = new byte[256];
    private final List<byte[]> writes = new ArrayList<>();
    private final List<Integer> reads = new ArrayList<>();
    private int failedRegister = -1;
    private boolean shortRead;
    private boolean failWrites;

    RegisterDevice(int address) {
      this.address = address;
      image[0x0f] = 0x32;
      image[0x21] = 0x10;
      image[0x23] = 0x10;
      image[0x24] = 3;
    }

    RegisterDevice axes(int x, int y, int z) {
      image[0x29] = (byte) x;
      image[0x2b] = (byte) y;
      image[0x2d] = (byte) z;
      return this;
    }

    @Override public void close() { }
    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return address; }
    @Override public int write(int value) { throw new AssertionError("Expected a register write"); }
    @Override public int write(byte[] data, int offset, int length) { throw new AssertionError("Expected a register write"); }
    @Override public int writeRegister(int register, byte[] data) {
      if (failWrites) throw new Pi4JException("I2C write failure");
      byte[] command = new byte[data.length + 1];
      command[0] = (byte) register;
      System.arraycopy(data, 0, command, 1, data.length);
      writes.add(command);
      System.arraycopy(data, 0, image, register, data.length);
      return data.length;
    }
    @Override public int read(byte[] data, int offset, int length) { throw new AssertionError("Expected a register read"); }
    @Override public int readRegister(int register) {
      reads.add(register);
      if (register == failedRegister) throw new Pi4JException("I2C read failure");
      return image[register] & 255;
    }
    @Override public int readRegister(int register, byte[] data, int offset, int length) {
      reads.add(register);
      if (register == failedRegister) throw new Pi4JException("I2C read failure");
      if (shortRead) return 0;
      System.arraycopy(image, register, data, offset, length);
      return length;
    }
    @Override public int read() { throw new AssertionError("Expected a register read"); }
  }
}
