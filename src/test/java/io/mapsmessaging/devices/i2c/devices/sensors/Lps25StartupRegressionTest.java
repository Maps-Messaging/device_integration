package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Sensor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Lps25StartupRegressionTest {
  @Test
  void lps25RequiresFirstReadySampleAndResetsConsecutiveMisses() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0x0f] = (byte) 0xbd;
    Lps25Sensor sensor = new Lps25Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
    assertTrue(sensor.getReadings().get(1).getValue().hasError());
    int resets = device.resets;
    for (int index = 0; index < 25; index++) {
      device.image[0x27] = 2;
      assertFalse(sensor.getReadings().get(0).getValue().hasError());
      device.image[0x27] = 0;
      assertFalse(sensor.getReadings().get(0).getValue().hasError());
    }
    assertEquals(resets, device.resets);
  }

  @Test
  void lps25SuccessfulSamplesBreakTheMissingDataStreak() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0x0f] = (byte) 0xbd;
    Lps25Sensor sensor = new Lps25Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    int resets = device.resets;
    for (int index = 0; index < 25; index++) {
      device.image[0x27] = 2;
      assertFalse(sensor.getReadings().get(0).getValue().hasError());
      device.image[0x27] = 0;
      assertFalse(sensor.getReadings().get(0).getValue().hasError());
    }
    assertEquals(resets, device.resets);
  }
}
