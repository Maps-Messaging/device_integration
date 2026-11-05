package io.mapsmessaging.devices.i2c.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.mapsmessaging.devices.i2c.RegisterBackedI2cDevice;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Device;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Device;
import org.junit.jupiter.api.Test;

class I2cExpanderDatasheetTest {

  @Test
  void mcp23017ClearingOneBitPreservesOtherBits() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);
    gpio.setOutput(0);
    gpio.setOutput(1);
    assertEquals(0xfc, bus.register(0x00));
    gpio.setInput(0);
    assertEquals(0xfd, bus.register(0x00));
  }

  @Test
  void mcp23017InterruptUsesDefaultComparisonAndCorrectPolarity() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);

    gpio.setOnHigh(2);
    assertEquals(0, bus.register(0x06) & 0x04);
    assertEquals(0x04, bus.register(0x08) & 0x04);

    gpio.setOnLow(2);
    assertEquals(0x04, bus.register(0x06) & 0x04);
    assertEquals(0x04, bus.register(0x08) & 0x04);
  }

  @Test
  void mcp23017OutputUsesLatchInsteadOfPhysicalInputRegister() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);

    gpio.setHigh(0);
    assertEquals(1, bus.register(0x14) & 1);
    bus.setRegister(0x12, 0); // Output held low externally, unlike the output latch.
    assertFalse(gpio.isSet(0));
    gpio.setHigh(1);
    assertEquals(3, bus.register(0x14) & 3);
  }

  @Test
  void pca9685PrescaleOverwritesPreviouslySetBitsAndUsesNxpRounding() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x40);
    bus.setRegister(0xfe, 0xff);
    Pca9685Device device = new Pca9685Device(bus);

    device.setPWMFrequency(50f);
    assertEquals(121, bus.register(0xfe));
    assertEquals(0, bus.register(0x00) & 0x10); // Device awake after updating prescale.
  }
}
