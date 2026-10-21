package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.InterruptRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.values.InterruptReason;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LightningStartupRegressionTest {
  @Test
  void interruptReasonRetainsItsPublicNoCheckedExceptionSignature() throws Exception {
    assertEquals(0, InterruptRegister.class.getMethod("getInterruptReason").getExceptionTypes().length);
  }

  @Test
  void lightningReasonRefreshesAfterConstruction() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(3, 0);
    InterruptRegister register = new InterruptRegister(device.bus());
    device.put(3, 8);
    assertEquals(InterruptReason.INT_L, register.getInterruptReason());
  }
}
