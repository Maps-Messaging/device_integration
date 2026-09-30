package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.bno055.BNO055Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers.OdrRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.OdrData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;
import io.mapsmessaging.devices.sensorreadings.Orientation;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalSensorMotionTest {
  @Test void bnoEulerDecodesSignedAxesInSixteenCountsPerDegree() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xa0).put(0x1a, 0xa0, 5, 0xf0, 0xff, 0xd0, 2);
    try (BNO055Sensor sensor = new BNO055Sensor(device)) {
      Orientation result = sensor.getEuler();
      assertEquals(90.0, result.getX());
      assertEquals(-1.0, result.getY());
      assertEquals(45.0, result.getZ());
    }
  }
  @Test void bnoAccelerationGravityAndMagneticFieldUseTheirPhysicalScales() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xa0)
        .put(8, 0x64, 0, 0x38, 0xff, 0xd5, 3)
        .put(0x0e, 0x10, 0, 0xe0, 0xff, 0x30, 0)
        .put(0x28, 0x32, 0, 0xce, 0xff, 0, 0)
        .put(0x2e, 0, 0, 0, 0, 0xd5, 3);
    try (BNO055Sensor sensor = new BNO055Sensor(device)) {
      assertVector(sensor.getAccelerometer(), 1, -2, 9.81);
      assertVector(sensor.getMagnetometer(), 1, -2, 3);
      assertVector(sensor.getLinearAcceleration(), .5, -.5, 0);
      assertVector(sensor.getGravity(), 0, 0, 9.81);
    }
  }
  @Test void bnoQuaternionDecodesSignedQ14Components() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xa0).put(0x20, 0, 0x40, 0, 0xe0, 0, 0x10, 0, 0);
    try (BNO055Sensor sensor = new BNO055Sensor(device)) {
      assertArrayEquals(new float[]{1, -.5f, .25f, 0}, sensor.getQuaternion());
    }
  }
  @Test void bnoRejectsWrongHardwareIdentity() {
    assertThrows(IOException.class, () -> new BNO055Sensor(new SensorRegisterDevice().put(0, 0x55)));
  }
  @Test void bnoDoesNotPublishVectorOnTransportFailure() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xa0);
    try (BNO055Sensor sensor = new BNO055Sensor(device)) {
      device.failedRegister = 0x1a;
      assertThrows(IOException.class, sensor::getEuler);
    }
  }
  @Test void msaOutputRateAndAxisEnablesRemainIndependent() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x10, 0xa0);
    OdrRegister register = new OdrRegister(device.bus());
    register.setOdr(Odr.HERTZ_125);
    assertEquals(0xa7, device.value(0x10));
    register.disableYAxis(true);
    assertEquals(0xe7, device.value(0x10));
    register.disableXAxis(false);
    assertEquals(0x67, device.value(0x10));
    assertEquals(Odr.HERTZ_125, register.getOdr());
  }
  @Test void msaOutputRateConfigurationUpdatesAllAxisBits() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x10, 0xea);
    OdrRegister register = new OdrRegister(device.bus());
    assertTrue(register.fromData(new OdrData(Odr.HERTZ_250, false, true, false)));
    assertEquals(0x48, device.value(0x10));
    OdrData result = (OdrData)register.toData();
    assertEquals(Odr.HERTZ_250, result.getOdr());
    assertFalse(result.isXAxisDisabled());
    assertTrue(result.isYAxisDisabled());
    assertFalse(result.isZAxisDisabled());
  }
  private static void assertVector(Orientation value, double x, double y, double z) {
    assertEquals(x, value.getX(), .00001);
    assertEquals(y, value.getY(), .00001);
    assertEquals(z, value.getZ(), .00001);
  }
}
