package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.ThresholdData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalSensorLightningTest {
  @Test void thresholdsPreserveReservedBitAndOtherFieldAcrossUpdates() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(1, 0xa3);
    ThresholdRegister register = new ThresholdRegister(device.bus());
    register.setWatchdogThreshold(9);
    assertEquals(0xa9, device.value(1));
    register.setNoiseFloorLevel(5);
    assertEquals(0xd9, device.value(1));
    assertEquals(9, register.getWatchdogThreshold());
    assertEquals(5, register.getNoiseFloorLevel());
  }
  @Test void thresholdConfigurationWritesBothIndependentFields() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(1, 0x80);
    ThresholdRegister register = new ThresholdRegister(device.bus());
    assertTrue(register.fromData(new ThresholdData(11, 6)));
    assertEquals(0xeb, device.value(1));
    ThresholdData result = (ThresholdData)register.toData();
    assertEquals(11, result.getWatchdogThreshold());
    assertEquals(6, result.getNoiseFloorLevel());
  }
  @Test void disturberMaskAndDividerPreserveInterruptReason() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(3, 0x88);
    InterruptRegister register = new InterruptRegister(device.bus());
    register.setMaskDisturberEnabled(true);
    assertEquals(0xa8, device.value(3));
    register.setEnergyDivRatio(1);
    assertEquals(0x68, device.value(3));
    register.setMaskDisturberEnabled(false);
    assertEquals(0x48, device.value(3));
  }
  @Test void distanceMasksUnrelatedBitsAndMapsOutOfRangeSentinel() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(7, 0xd4);
    DistanceRegister register = new DistanceRegister(device.bus());
    assertEquals(20, register.getDistanceEstimation());
    device.put(7, 0xff);
    assertEquals(32767, register.getDistanceEstimation());
  }
  @Test void clearingStatisticsPreservesSpikeRejectionAndMinimumStrikes() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(2, 0x25);
    LightningRegister register = new LightningRegister(device.bus());
    register.setClearStatisticsEnabled(true);
    assertEquals(0x65, device.value(2));
    register.setClearStatisticsEnabled(false);
    assertEquals(0x25, device.value(2));
  }
}
