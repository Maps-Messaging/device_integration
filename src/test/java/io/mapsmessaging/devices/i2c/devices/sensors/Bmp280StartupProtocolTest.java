package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.bmp280.BMP280Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.bmp280.BMP280Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bmp280.registers.PressureRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bmp280.registers.TemperatureRegister;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bmp280StartupProtocolTest {
  @Test
  void bmpUsesBoschIdentityCalibrationAndReferenceCompensation() throws Exception {
    RegisterDevice device = bmp();
    BMP280Sensor sensor = new FastBmp(device);
    assertArrayEquals(new int[]{0x76, 0x77}, new BMP280Controller().getAddressRange());
    assertTrue(new BMP280Controller().detect(device));
    assertEquals(0xb6, device.registers.get(0xe0)[0] & 255);
    assertEquals(25.08f, sensor.getTemperature(), .01f);
    assertEquals(1006.5325f, sensor.getPressure(), .01f);
    assertEquals(0x25, device.registers.get(0xf4)[0] & 255);
  }

  @Test
  void bmpRejectsWrongIdentityInvalidCalibrationAndIncompleteConversion() throws Exception {
    RegisterDevice wrong = bmp().put(0xd0, 0x58 + 1);
    assertThrows(IOException.class, () -> new FastBmp(wrong));
    assertFalse(new BMP280Controller().detect(wrong));
    RegisterDevice invalid = bmp();
    invalid.registers.get(0x88)[6] = 0;
    invalid.registers.get(0x88)[7] = 0;
    assertThrows(IOException.class, () -> new FastBmp(invalid));
    RegisterDevice busy = bmp();
    BMP280Sensor sensor = new FastBmp(busy);
    busy.put(0xf3, 8);
    assertThrows(IOException.class, sensor::getTemperature);
    busy.put(0xf3, 0).put(0xf7, 0x80, 0, 0, 0x80, 0, 0);
    assertThrows(IOException.class, sensor::getPressure);
  }

  @Test
  void bmpRawRegisterHelpersDecodeBigEndianTwentyBitValues() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0xf7, 0x65, 0x5a, 0xcf).put(0xfa, 0x7e, 0xed, 0x0f);
    PressureRegister pressure = new PressureRegister(device.bus());
    TemperatureRegister temperature = new TemperatureRegister(device.bus());
    assertEquals(415148d, pressure.getRawPressure());
    assertEquals(519888d, temperature.getRawTemperature());
  }

  @Test
  void truncatedBmpReadsFailAndRetryImmediately() throws Exception {
    RegisterDevice bmp = bmp();
    BMP280Sensor sensor = new FastBmp(bmp);
    bmp.put(0xf7, 0x65);
    assertThrows(IOException.class, sensor::getTemperature);
    bmp.put(0xf7, 0x65, 0x5a, 0xc0, 0x7e, 0xed, 0);
    assertEquals(25.08f, sensor.getTemperature(), .01f);
  }

  private static RegisterDevice bmp() {
    RegisterDevice device = new RegisterDevice().put(0xd0, 0x58).put(0xf3, 0).put(0xf7, 0x65, 0x5a, 0xc0, 0x7e, 0xed, 0);
    int[] coefficients = {27504, 26435, -1000, 36477, -10685, 3024, 2855, 140, -7, 15500, -14600, 6000};
    int[] bytes = new int[24];
    for (int index = 0; index < coefficients.length; index++) {
      bytes[index * 2] = coefficients[index] & 255;
      bytes[index * 2 + 1] = (coefficients[index] >>> 8) & 255;
    }
    return device.put(0x88, bytes);
  }

  private static class FastBmp extends BMP280Sensor {
    FastBmp(AddressableDevice device) throws IOException {
      super(device);
    }
    @Override
    public void delay(int milliseconds) {
      // Register fixtures have no physical conversion latency.
    }
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
