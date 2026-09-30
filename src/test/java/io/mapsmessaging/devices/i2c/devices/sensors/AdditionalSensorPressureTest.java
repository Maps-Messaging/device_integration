package io.mapsmessaging.devices.i2c.devices.sensors;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalSensorPressureTest {
  @Test void lps25DiscoveryAcceptsItsIdentityAndRejectsOtherDevices() {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x0f, 0xbd);
    var controller = new io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Controller();
    assertTrue(controller.detect(device));
    device.put(0x0f, 0xb1);
    assertFalse(controller.detect(device));
    device.failedRegister = 0x0f;
    assertFalse(controller.detect(device));
    assertEquals(java.util.List.of(0x0f, 0x0f, 0x0f), device.reads);
  }

  @Test void lps25ConvertsLittleEndianPressureAndRefreshesEachSample() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0xa8, 0, 0x50, 0x3e);
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.PressureRegister(device.bus());
    assertEquals(997.0f, register.getPressure());
    device.put(0xa8, 0, 0x10, 0);
    assertEquals(1.0f, register.getPressure());
    assertEquals(java.util.List.of(0xa8, 0xa8), device.reads);
  }
  @Test void lps25SignExtendsNegativePressure() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0xa8, 0, 0xf0, 0xff);
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.PressureRegister(device.bus());
    assertEquals(-1.0f, register.getPressure());
  }
  @Test void lps25TemperatureUsesOffsetAnd480CountsPerDegree() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0xab, 0xe0, 1);
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.TemperatureRegister(device.bus());
    assertEquals(43.5f, register.getTemperature());
    device.put(0xab, 0, 0);
    assertEquals(42.5f, register.getTemperature());
  }
  @Test void lps35PressureUses4096CountsPerHectopascal() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x28, 0, 0x54, 0x3f);
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.PressureRegister(device.bus());
    assertEquals(1013.25f, register.getPressure());
  }
  @Test void lps35TemperatureUsesHundredthsOfDegree() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x2b, 0xc4, 9);
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.TemperatureRegister(device.bus());
    assertEquals(25.0f, register.getTemperature());
  }
  @Test void pressureTransportFailureDoesNotBecomeZeroPressure() {
    SensorRegisterDevice device = new SensorRegisterDevice();
    device.failedRegister = 0x28;
    var register = new io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.PressureRegister(device.bus());
    assertThrows(IOException.class, register::getPressure);
  }
}
