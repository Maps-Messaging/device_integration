package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i.Pmsa003iSensor;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Pmsa003iStartupRegressionTest {
  @Test
  void particulateRejectsInvalidFramesAndRetriesImmediately() throws Exception {
    byte[] valid = particulateFrame();
    byte[][] invalid = {Arrays.copyOf(valid, 10), valid.clone(), valid.clone(), valid.clone(), valid.clone()};
    invalid[1][0] = 0;
    invalid[2][3] = 27;
    invalid[3][31] ^= 1;
    invalid[4][29] = 1;
    invalid[4][31]++;
    for (byte[] rejected : invalid) {
      ScriptedI2CDevice bus = new ScriptedI2CDevice(0x12, rejected, valid);
      Pmsa003iSensor sensor = new Pmsa003iSensor(bus);
      assertThrows(IOException.class, sensor::getVersion);
      assertEquals(3, sensor.getVersion());
    }
  }

  private static byte[] particulateFrame() {
    byte[] bytes = new byte[32];
    bytes[0] = 0x42;
    bytes[1] = 0x4d;
    bytes[3] = 28;
    bytes[28] = 3;
    int checksum = 0;
    for (int index = 0; index < 30; index++) {
      checksum += bytes[index] & 0xff;
    }
    bytes[30] = (byte) (checksum >>> 8);
    bytes[31] = (byte) checksum;
    return bytes;
  }
}
