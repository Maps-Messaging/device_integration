package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.am2315.AM2315Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.am2320.AM2320Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmFamilyStartupRegressionTest {
  @Test
  void am2315DecodesTemperatureAndReportsConnected() throws Exception {
    AM2315Sensor sensor = new FastAm2315(new ScriptedI2CDevice(0x5c, frame(3, 4, 1, 244, 1, 250)));
    assertEquals(50.6f, sensor.getTemperature(), .001f);
    assertTrue(sensor.isConnected());
  }

  @Test
  void am2315RejectsInvalidCrcAndShortFrames() {
    assertThrows(IOException.class, () -> new FastAm2315(new ScriptedI2CDevice(0x5c, new byte[]{3, 4, 0, 0, 0, 0, 0, 0})));
    assertThrows(IOException.class, () -> new FastAm2315(new ScriptedI2CDevice(0x5c, new byte[]{3, 4})));
  }

  @Test
  void am2320RejectsInvalidInitialFrameAndReportsConnected() throws Exception {
    assertThrows(IOException.class, () -> new FastAm2320(new ScriptedI2CDevice(0x5c, new byte[8])));
    AM2320Sensor sensor = new FastAm2320(new ScriptedI2CDevice(0x5c, frame(3, 4, 1, 244, 0, 250)));
    assertTrue(sensor.isConnected());
    assertEquals(25f, sensor.getTemperature());
  }

  @Test
  void am2320RejectsShortResponseEvenWithRetainedRequestBytes() {
    assertThrows(IOException.class, () -> new FastAm2320(new ScriptedI2CDevice(0x5c, new byte[]{3, 4})));
  }

  private static byte[] frame(int... fields) {
    byte[] result = new byte[fields.length + 2];
    int crc = 0xffff;
    for (int index = 0; index < fields.length; index++) {
      result[index] = (byte) fields[index];
      crc ^= fields[index];
      for (int bit = 0; bit < 8; bit++) {
        crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : crc >>> 1;
      }
    }
    result[fields.length] = (byte) crc;
    result[fields.length + 1] = (byte) (crc >>> 8);
    return result;
  }

  private static class FastAm2315 extends AM2315Sensor {
    FastAm2315(AddressableDevice device) throws IOException {
      super(device);
    }
    @Override
    public void delay(int milliseconds) {
      // Hardware waits are controlled by each test.
    }
  }

  private static class FastAm2320 extends AM2320Sensor {
    FastAm2320(AddressableDevice device) throws IOException {
      super(device);
    }
    @Override
    public void delay(int milliseconds) {
      // Hardware waits are controlled by each test.
    }
  }
}
