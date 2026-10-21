package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.bno055.BNO055Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.BNO055Sensor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bno055StartupRegressionTest {
  @Test
  void bnoDetectsRealIdAndReportsLiveConnection() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0] = (byte) 0xa0;
    assertTrue(new BNO055Controller().detect(device));
    BNO055Sensor sensor = new BNO055Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertTrue(sensor.isConnected());
    device.image[0] = 0;
    assertFalse(sensor.isConnected());
  }

  @Test
  void bnoMountedConnectionTracksHardwareId() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.image[0] = (byte) 0xa0;
    BNO055Sensor sensor = new BNO055Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertTrue(sensor.isConnected());
  }
}
