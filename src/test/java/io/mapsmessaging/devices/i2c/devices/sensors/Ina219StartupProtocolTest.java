package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.ina219.Ina219Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.Ina219Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers.ADCResolution;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ina219StartupProtocolTest {
  @Test
  void inaInitializesBothAdcsAndWaitsForConversionBeforePublishing() throws Exception {
    RegisterDevice device = new RegisterDevice().word(2, 0);
    Ina219Sensor sensor = new Ina219Sensor(device);
    sensor.initialize();
    assertEquals(0x399f, device.word(0));
    assertEquals(4096, device.word(5));
    assertThrows(IOException.class, sensor::getCurrent_mA);
    assertThrows(IOException.class, sensor::getPower);
    device.word(2, (3000 << 3) | 2).word(4, 0xff9c).word(1, 0xff9c).word(3, 50);
    assertEquals(12000, sensor.getBusVoltage());
    assertEquals(-10f, sensor.getCurrent_mA(), .001f);
    assertEquals(-1d, sensor.getShuntVoltage(), .001d);
    assertEquals(100f, sensor.getPower(), .001f);
  }

  @Test
  void inaRejectsIgnoredConfigurationAndCalibrationWrites() {
    RegisterDevice config = new RegisterDevice();
    config.ignoredWrite = 0;
    assertThrows(IOException.class, () -> new Ina219Sensor(config).initialize());
    RegisterDevice calibration = new RegisterDevice();
    calibration.ignoredWrite = 5;
    assertThrows(IOException.class, () -> new Ina219Sensor(calibration).initialize());
  }

  @Test
  void inaUsesEachBusAdcResolutionAndAppliesControllerUpdates() throws Exception {
    RegisterDevice device = new RegisterDevice();
    Ina219Controller controller = new Ina219Controller(device);
    ADCResolution[] resolutions = {ADCResolution.RES_9BIT, ADCResolution.RES_10BIT, ADCResolution.RES_11BIT, ADCResolution.RES_12BIT};
    for (int index = 0; index < resolutions.length; index++) {
      String payload = "{\"adcResolution\":\"" + resolutions[index].name() + "\"}";
      controller.updateDeviceConfiguration(payload.getBytes(StandardCharsets.UTF_8));
      assertEquals(index << 7, device.word(0) & 0x0780);
    }
  }

  @Test
  void truncatedInaConfigurationReadFails() {
    RegisterDevice ina = new RegisterDevice().put(0, 0);
    ina.ignoredWrite = 0;
    assertThrows(IOException.class, () -> new Ina219Sensor(ina).initialize());
  }

  private static class RegisterDevice implements AddressableDevice {
    final Map<Integer, byte[]> registers = new HashMap<>();
    int ignoredWrite = -1;
    RegisterDevice put(int address, int... values) {
      byte[] bytes = new byte[values.length];
      for (int index = 0; index < values.length; index++) {
        bytes[index] = (byte) values[index];
      }
      registers.put(address, bytes);
      return this;
    }
    RegisterDevice word(int address, int value) {
      return put(address, value >>> 8, value);
    }
    int word(int address) {
      byte[] bytes = registers.getOrDefault(address, new byte[2]);
      return ((bytes[0] & 255) << 8) | (bytes[1] & 255);
    }
    @Override
    public void close() {
      // No native resources in this register fixture.
    }
    @Override
    public int getBus() {
      return 1;
    }
    @Override
    public int getDevice() {
      return 0x76;
    }
    @Override
    public int write(int value) {
      return 1;
    }
    @Override
    public int write(byte[] data, int offset, int length) {
      return length;
    }
    @Override
    public int writeRegister(int address, byte[] data) {
      if (address != ignoredWrite) {
        registers.put(address, Arrays.copyOf(data, data.length));
      }
      return data.length;
    }
    @Override
    public int read(byte[] data, int offset, int length) {
      return -1;
    }
    @Override
    public int readRegister(int address) {
      return registers.getOrDefault(address, new byte[1])[0] & 255;
    }
    @Override
    public int readRegister(int address, byte[] data, int offset, int length) {
      byte[] bytes = registers.getOrDefault(address, new byte[length]);
      int count = Math.min(length, bytes.length);
      System.arraycopy(bytes, 0, data, offset, count);
      return count;
    }
    @Override
    public int read() {
      return -1;
    }
  }
}
