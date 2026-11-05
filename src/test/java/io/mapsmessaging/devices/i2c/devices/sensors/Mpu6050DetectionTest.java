package io.mapsmessaging.devices.i2c.devices.sensors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.Ds3231Controller;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Mpu6050DetectionTest {
  @Test
  void imuIdentityIsReadOnlyAndIndependentOfAd0Address() {
    for (int address : new int[]{0x68, 0x69}) {
      Hardware device = new Hardware(address).imu();
      I2CDeviceController imu = imu();
      assertTrue(imu.canDetect());
      assertArrayEquals(new int[]{0x68, 0x69}, imu.getAddressRange());
      assertTrue(imu.detect(device));
      assertFalse(new Ds3231Controller().detect(device));
      assertTrue(device.writes.isEmpty());
      assertFalse(device.closed);
    }
  }

  @Test
  void rtcProbeAcceptsClockModesAndStoppedOscillatorWithoutClosingOrWriting() {
    for (int hour : new int[]{0x00, 0x23, 0x41, 0x52, 0x72}) {
      Hardware device = new Hardware(0x68).rtc();
      device.image[2] = (byte) hour;
      device.image[15] = (byte) 0x8f;
      assertTrue(new Ds3231Controller().detect(device));
      assertFalse(imu().detect(device));
      assertTrue(device.writes.isEmpty());
      assertFalse(device.closed);
    }
  }

  @Test
  void rtcProbeRejectsMalformedRegistersAndMpuEvenWithPlausibleCalendar() {
    for (int[] invalid : new int[][]{{0,0x6a},{1,0x60},{2,0x24},{2,0x40},{3,0},{4,0x31},{5,0},{6,0xfa},{15,0x10},{18,1},{0x75,0x68}}) {
      Hardware device = new Hardware(0x68).rtc();
      device.image[invalid[0]] = (byte) invalid[1];
      assertFalse(new Ds3231Controller().detect(device), Arrays.toString(invalid));
      assertTrue(device.writes.isEmpty());
      assertFalse(device.closed);
    }
    Hardware zero = new Hardware(0x68);
    assertFalse(new Ds3231Controller().detect(zero));
    Arrays.fill(zero.image, (byte) 0xff);
    assertFalse(new Ds3231Controller().detect(zero));
  }

  @Test
  void rtcMountRetainsAddressAndPreservesSettings() throws Exception {
    Hardware device = new Hardware(0x68).rtc();
    byte[] before = device.image.clone();
    I2CDeviceController controller = new Ds3231Controller().mount(device);
    assertEquals(0x68, controller.getMountedAddress());
    assertArrayEquals(before, device.image);
    assertTrue(device.writes.isEmpty());
    assertFalse(device.closed);
  }

  @Test
  void probesRejectFailuresAndShortRtcReadsWithoutSideEffects() {
    Hardware device = new Hardware(0x68).rtc();
    device.shortRead = true;
    assertFalse(new Ds3231Controller().detect(device));
    device.shortRead = false;
    device.failedRegister = 0x75;
    assertFalse(imu().detect(device));
    assertTrue(new Ds3231Controller().detect(device));
    device.failedRegister = 0;
    assertFalse(new Ds3231Controller().detect(device));
    assertFalse(device.closed);
    assertTrue(device.writes.isEmpty());
  }

  @Test
  void rtcStillDetectsWhenBackendRejectsTheMpuOnlyRegister() {
    Hardware device = new Hardware(0x68).rtc();
    device.unsupportedIdentity = true;
    assertTrue(new Ds3231Controller().detect(device));
    assertFalse(imu().detect(device));
    assertTrue(device.writes.isEmpty());
    assertFalse(device.closed);
  }

  @Test
  void rtcProbeValidatesCenturyAndLeapDay() {
    Hardware device = new Hardware(0x68).rtc();
    device.image[4] = 0x29;
    device.image[6] = 0;
    assertTrue(new Ds3231Controller().detect(device)); // 2000-02-29.
    device.image[5] = (byte) 0x82;
    assertFalse(new Ds3231Controller().detect(device)); // 2100 is not a leap year.
    device.image[6] = 0x24;
    assertTrue(new Ds3231Controller().detect(device)); // 2124-02-29.
  }

  @Test
  void wrongIdentityCannotConfigureRtcAsImu() {
    Hardware rtc = new Hardware(0x68).rtc();
    assertThrows(IOException.class, () -> imu().mount(rtc));
    assertTrue(rtc.writes.isEmpty());
    assertFalse(rtc.closed);
    for (int identity : new int[]{0x69, 0x70, 0xff}) {
      Hardware wrongChip = new Hardware(0x68).imu();
      wrongChip.image[0x75] = (byte) identity;
      assertFalse(imu().detect(wrongChip));
      assertThrows(IOException.class, () -> imu().mount(wrongChip));
      assertTrue(wrongChip.writes.isEmpty());
    }
  }

  @Test
  void mountConfiguresKnownScalesAndLiveConnection() throws Exception {
    Hardware device = new Hardware(0x69).imu();
    I2CDeviceController controller = imu().mount(device);
    assertEquals(0x69, controller.getMountedAddress());
    assertEquals(1, device.image[0x6b]);
    assertEquals(0, device.image[0x6c]);
    assertEquals(3, device.image[0x1a]);
    assertEquals(9, device.image[0x19]);
    assertEquals(0, device.image[0x1b]);
    assertEquals(0, device.image[0x1c]);
    assertTrue(controller.getDevice().isConnected());
    device.failedRegister = 0x75;
    assertFalse(controller.getDevice().isConnected());
    device.failedRegister = -1;
    device.image[0x75] = 0x70;
    assertFalse(controller.getDevice().isConnected());
  }

  @Test
  void signedMotionAndTemperatureComeFromOneWindowPerJsonReport() throws Exception {
    Hardware device = new Hardware(0x68).imu();
    I2CDeviceController controller = imu().mount(device);
    device.words(0x4000, -16384, -32768, 340, 131, -131, 0);
    device.reads.clear();
    JsonObject data = json(controller);
    assertEquals(9.80665, data.get("x_acceleration").getAsDouble(), 0.0001);
    assertEquals(-9.80665, data.get("y_acceleration").getAsDouble(), 0.0001);
    assertEquals(-19.6133, data.get("z_acceleration").getAsDouble(), 0.0001);
    assertEquals(37.53, data.get("temperature").getAsDouble(), 0.001);
    assertEquals(0.0174533, data.get("x_angular_velocity").getAsDouble(), 0.000001);
    assertEquals(-0.0174533, data.get("y_angular_velocity").getAsDouble(), 0.000001);
    assertEquals(0, data.get("z_angular_velocity").getAsDouble());
    assertEquals(List.of(0x3b), device.reads);
    device.words(0, 0, 16384, -340, 0, 0, 0);
    assertEquals(35.53, json(controller).get("temperature").getAsDouble(), 0.001);
    assertEquals(9.80665, json(controller).get("z_acceleration").getAsDouble(), 0.0001);
    device.words(8192, -8192, 32767, 170, 65, -65, -32768);
    JsonObject fractions = json(controller);
    assertEquals(4.90333, fractions.get("x_acceleration").getAsDouble(), 0.00001);
    // Existing sensor formatting uses Math.round: negative half-ties round toward positive infinity.
    assertEquals(-4.90332, fractions.get("y_acceleration").getAsDouble(), 0.00001);
    assertEquals(19.61270, fractions.get("z_acceleration").getAsDouble(), 0.00001);
    assertEquals(37.03, fractions.get("temperature").getAsDouble(), 0.001);
    assertEquals(0.008660, fractions.get("x_angular_velocity").getAsDouble(), 0.000001);
    assertEquals(-0.008660, fractions.get("y_angular_velocity").getAsDouble(), 0.000001);
    assertEquals(-4.365721, fractions.get("z_angular_velocity").getAsDouble(), 0.000001);
    assertNotNull(controller.getSchema());
  }

  @Test
  void incompleteSampleCannotPublishStaleMotionAndDirectReadingsRecover() throws Exception {
    Hardware device = new Hardware(0x68).imu();
    I2CDeviceController controller = imu().mount(device);
    device.words(16384, 0, 0, 0, 0, 0, 0);
    json(controller);
    device.shortRead = true;
    controller.setRaiseExceptionOnError(true);
    assertFalse(json(controller).has("x_acceleration"));
    device.shortRead = false;
    device.words(0, 0, 0, 0, 0, 0, 0);
    io.mapsmessaging.devices.deviceinterfaces.Sensor sensor = (io.mapsmessaging.devices.deviceinterfaces.Sensor) controller.getDevice();
    assertEquals(0f, sensor.getReadings().getFirst().getValue().getResult());
    device.words(16384, 0, 0, 0, 0, 0, 0);
    assertEquals(9.80665f, (Float) sensor.getReadings().getFirst().getValue().getResult(), 0.0001f);
  }

  private static I2CDeviceController imu() {
    for (I2CDeviceController controller : ServiceLoader.load(I2CDeviceController.class)) {
      if (controller.getName().equals("MPU6050")) return controller;
    }
    return fail("MPU6050 controller must be registered");
  }

  private static JsonObject json(I2CDeviceController controller) throws IOException {
    return JsonParser.parseString(new String(controller.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static final class Hardware implements AddressableDevice {
    private final byte[] image = new byte[256];
    private final List<Integer> reads = new ArrayList<>();
    private final List<byte[]> writes = new ArrayList<>();
    private final int address;
    private int failedRegister = -1;
    private boolean shortRead;
    private boolean unsupportedIdentity;
    private boolean closed;
    Hardware(int address) { this.address = address; }
    Hardware imu() { image[0x75] = 0x68; image[0x6b] = 0x40; return this; }
    Hardware rtc() {
      int[] data = {0x56,0x34,0x12,4,0x28,0x02,0x24,0,0,0,0,0,0,0,0x1c,0x80,0,25,0x40};
      for (int index = 0; index < data.length; index++) image[index] = (byte) data[index];
      return this;
    }
    void words(int... data) {
      for (int index = 0; index < data.length; index++) {
        image[0x3b + index * 2] = (byte) (data[index] >> 8);
        image[0x3c + index * 2] = (byte) data[index];
      }
    }
    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return address; }
    @Override public void close() { closed = true; }
    @Override public int write(int value) { throw new AssertionError("Expected register I/O"); }
    @Override public int write(byte[] data, int offset, int length) { throw new AssertionError("Expected register I/O"); }
    @Override public int read(byte[] data, int offset, int length) { throw new AssertionError("Expected register I/O"); }
    @Override public int read() { throw new AssertionError("Expected register I/O"); }
    @Override public int writeRegister(int register, byte[] data) {
      writes.add(new byte[]{(byte) register, data[0]});
      System.arraycopy(data, 0, image, register, data.length);
      return data.length;
    }
    @Override public int readRegister(int register) {
      reads.add(register);
      if (register == 0x75 && unsupportedIdentity) throw new com.pi4j.exception.Pi4JException("Unsupported register");
      return register == failedRegister ? -1 : image[register] & 0xff;
    }
    @Override public int readRegister(int register, byte[] data, int offset, int length) {
      reads.add(register);
      if (register == failedRegister) return -1;
      System.arraycopy(image, register, data, offset, length);
      return shortRead ? length - 1 : length;
    }
  }
}
