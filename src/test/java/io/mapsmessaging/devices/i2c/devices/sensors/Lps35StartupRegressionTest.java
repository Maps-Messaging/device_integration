package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.lps35.Lps35Sensor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Lps35StartupRegressionTest {
  @Test
  void lps35RequiresReadySampleAfterMountAndRestartsAfterSoftReset() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0x0f] = (byte) 0xb1;
    Lps35Sensor sensor = new Lps35Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
    assertTrue(sensor.getReadings().get(1).getValue().hasError());
    device.image[0x27] = 3;
    assertFalse(sensor.getReadings().get(0).getValue().hasError());
    assertFalse(sensor.getReadings().get(1).getValue().hasError());
    sensor.softReset();
    assertEquals(0x10, device.image[0x10] & 0x70);
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
  }

  @Test
  void lps35SoftResetRestoresContinuousSampling() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0x0f] = (byte) 0xb1;
    Lps35Sensor sensor = new Lps35Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    sensor.softReset();
    assertEquals(0x10, device.image[0x10] & 0x70);
  }
}
