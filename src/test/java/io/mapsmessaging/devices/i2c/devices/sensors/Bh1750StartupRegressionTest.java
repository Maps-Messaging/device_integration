package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bh1750StartupRegressionTest {
  @Test
  void bh1750RejectsPrematureReadAndResetInvalidatesReadiness() throws Exception {
    Bh1750Sensor sensor = new FastBh1750(new ScriptedI2CDevice(0x23, new byte[]{0, 120}));
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
    Thread.sleep(200);
    assertFalse(sensor.getReadings().get(0).getValue().hasError());
    sensor.softReset();
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
  }

  @Test
  void bh1750RetriesImmediatelyAfterShortRead() throws Exception {
    Bh1750Sensor sensor = new FastBh1750(new ScriptedI2CDevice(0x23, new byte[]{0}, new byte[]{0, 120}));
    Thread.sleep(200);
    assertTrue(sensor.getReadings().get(0).getValue().hasError());
    assertFalse(sensor.getReadings().get(0).getValue().hasError());
  }

  private static class FastBh1750 extends Bh1750Sensor {
    FastBh1750(AddressableDevice device) throws IOException {
      super(device);
    }
    @Override
    public void delay(int milliseconds) {
      // Hardware waits are controlled by each test.
    }
  }
}
