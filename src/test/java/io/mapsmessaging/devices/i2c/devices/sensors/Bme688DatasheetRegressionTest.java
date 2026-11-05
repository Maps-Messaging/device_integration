package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.CalibrationData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.ControlGas0Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.ControlGas1Register;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.LargeValueRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.ValueRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.HeaterStep;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Bme688DatasheetRegressionTest {

  @Test
  void unsignedAdcBytesDoNotSignExtend() throws Exception {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x77,
        new byte[] {(byte) 0x80, 0, 0},
        new byte[] {(byte) 0x80, 0});
    TestSensor sensor = new TestSensor(device);

    assertEquals(0x80000, new LargeValueRegister(sensor, 0x1f, "pressure").getValue());
    assertEquals(0x8000, new ValueRegister(sensor, 0x25, "humidity").getValue());
  }

  @Test
  void signedCoefficientsRetainBothBytes() throws Exception {
    byte[] coefficients = new byte[42];
    coefficients[6] = (byte) 0xfe;
    coefficients[7] = (byte) 0xff;
    coefficients[8] = (byte) 0xff;
    coefficients[18] = (byte) 0x34;
    coefficients[19] = (byte) 0xff;
    coefficients[20] = (byte) 0x78;
    coefficients[21] = (byte) 0x56;
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x77,
        Arrays.copyOfRange(coefficients, 0, 23),
        Arrays.copyOfRange(coefficients, 23, 37),
        Arrays.copyOfRange(coefficients, 37, 42));
    CalibrationData data = new CalibrationData(new TestSensor(device));

    assertEquals(-2, data.getPressureCalibrationData().getParP2());
    assertEquals(-1, data.getPressureCalibrationData().getParP3());
    assertEquals(-204, data.getPressureCalibrationData().getParP8());
    assertEquals(0x5678, data.getPressureCalibrationData().getParP9());
  }

  @Test
  void gasModeSettersPreserveUnrelatedBits() throws Exception {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x77, new byte[] {0x25});
    ControlGas1Register register = new ControlGas1Register(new TestSensor(device));

    register.setNbConv(HeaterStep.NONE);
    assertArrayEquals(new byte[] {0x20}, device.writes().get(1));
    register.setRunGas(true);
    assertArrayEquals(new byte[] {0x20}, device.writes().get(3));
  }

  @Test
  void heaterControlBitIsActiveLow() throws Exception {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x77, new byte[] {0x08}, new byte[] {0x08});
    ControlGas0Register register = new ControlGas0Register(new TestSensor(device));

    assertFalse(register.isHeatOn());
    register.setHeatOn(true);
    assertArrayEquals(new byte[] {0}, device.writes().get(1));
  }

  private static final class TestSensor extends I2CDevice {
    TestSensor(AddressableDevice device) {
      super(device, LoggerFactory.getLogger(TestSensor.class));
    }

    @Override public boolean isConnected() { return true; }
    @Override public String getName() { return "BME688 test"; }
    @Override public String getDescription() { return "Register test"; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
  }
}
