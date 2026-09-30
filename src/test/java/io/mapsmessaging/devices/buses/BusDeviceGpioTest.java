package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Device;
import org.junit.jupiter.api.Test;

class BusDeviceGpioTest {
  @Test void physicalPinsDecodeBothPortsAndRefreshBetweenReads() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);
    bus.seed(0x12, 0x81, 0x42);
    assertTrue(gpio.isSet(0));
    assertTrue(gpio.isSet(7));
    assertTrue(gpio.isSet(9));
    assertTrue(gpio.isSet(14));
    assertFalse(gpio.isSet(8));
    bus.seed(0x12, 0, 0);
    assertFalse(gpio.isSet(7));
  }
  @Test void enabledPullupsAccumulateAcrossPortBoundary() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);
    gpio.enablePullUp(7);
    gpio.enablePullUp(8);
    gpio.enablePullUp(15);
    assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x81}, bus.image(0x0c, 2));
  }
  @Test void interruptFlagsReturnOrderedPinNumbersAndNoStaleFlags() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);
    bus.seed(0x0e, 0x81, 0x81);
    assertArrayEquals(new int[]{0, 7, 8, 15}, gpio.getInterrupted());
    bus.seed(0x0e, 0, 0);
    assertArrayEquals(new int[0], gpio.getInterrupted());
  }
  @Test void outputHighAccumulatesBitsAcrossPorts() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x20);
    Mcp23017Device gpio = new Mcp23017Device(bus);
    gpio.setHigh(0);
    gpio.setHigh(9);
    assertArrayEquals(new byte[]{1, 2}, bus.image(0x12, 2));
  }
}
