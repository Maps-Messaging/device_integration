package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers.TimingRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.TimingData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers.TemperatureRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers.ConcentrationRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers.SensorReadingRegister;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalSensorLightGasTest {
  @Test void timingConfigurationPreservesReservedBitsAndWritesGainManualIntegration() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0x81, 0xa0);
    TimingRegister register = new TimingRegister(device.bus());
    assertTrue(register.fromData(new TimingData(true, true, IntegrationTime.MS_402)));
    assertEquals(0xba, device.value(0x81));
    TimingData result = (TimingData)register.toData();
    assertTrue(result.isManual());
    assertTrue(result.isHighGain());
    assertEquals(IntegrationTime.MS_402, result.getIntegrationTime());
    register.setHighGain(false);
    assertEquals(0xaa, device.value(0x81));
    register.setManual(false);
    assertEquals(0xa2, device.value(0x81));
  }
  @Test void gravityTemperatureAtHalfScaleIs25DegreesAndSendsTemperatureCommand() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x87, 2, 0, 0, 0, 0, 0, 0x77);
    TemperatureRegister register = new TemperatureRegister(device.bus());
    assertEquals(25f, register.getTemperature(), .001f);
    assertArrayEquals(new byte[]{(byte)0xff, 1, (byte)0x87, 0, 0, 0, 0, 0, 0x78}, device.commands.get(0));
  }
  @Test void gravityTemperatureRejectsCorruptedChecksum() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x87, 2, 0, 0, 0, 0, 0, 0x76);
    assertTrue(Float.isNaN(new TemperatureRegister(device.bus()).getTemperature()));
  }
  @Test void gravityConcentrationRespectsDecimalPlacesAndRequestFraming() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x86, 4, 0xd2, 0, 2, 0, 0, 0xa2);
    assertEquals(12.34f, new ConcentrationRegister(device.bus()).getConcentration(), .0001f);
    assertArrayEquals(new byte[]{(byte)0xff, 1, (byte)0x86, 0, 0, 0, 0, 0, 0x79}, device.commands.get(0));
  }
  @Test void gravityCombinedReadingSharesOneSampleBetweenConcentrationAndTemperature() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0xff, 0x88, 4, 0xd2, 0, 1, 2, 0, 0x9f);
    SensorReadingRegister register = new SensorReadingRegister(device.bus());
    assertEquals(123.4f, register.getConcentration(), .001f);
    assertEquals(25f, register.getTemperature(), .001f);
    assertEquals(1, device.commands.size());
    assertArrayEquals(new byte[]{(byte)0xff, 1, (byte)0x88, 0, 0, 0, 0, 0, 0x77}, device.commands.get(0));
  }
}
