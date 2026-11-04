package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.gravity.GasSensor;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers.SensorReadingRegister;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GravityStartupRegressionTest {
  @Test
  void gravityRejectsInitialChecksumFailureAndAllowsImmediateRetry() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x88, 4, 0xd2, 0, 1, 2, 0, 0);
    SensorReadingRegister register = new SensorReadingRegister(device.bus());
    assertThrows(IOException.class, register::getConcentration);
    device.put(0, 0xff, 0x88, 4, 0xd2, 0, 1, 2, 0, 0x9f);
    assertEquals(123.4f, register.getConcentration(), .001f);
  }

  @Test
  void gravityRejectsUnknownSensorTypeAsIoFailure() {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x86, 0, 0, 0x7e, 0, 0, 0, 0xfc);
    assertThrows(IOException.class, () -> new GasSensor(device));
  }
}
