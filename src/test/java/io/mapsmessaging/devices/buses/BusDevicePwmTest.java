package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Device;
import org.junit.jupiter.api.Test;

class BusDevicePwmTest {
  @Test void channelPulseUsesFourLittleEndianBytesAtSelectedChannel() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x40);
    Pca9685Device pwm = new Pca9685Device(bus);
    pwm.setPWM(3, 0x123, 0xabc);
    assertArrayEquals(new byte[]{0x23, 1, (byte) 0xbc, 0x0a}, bus.image(0x12, 4));
    assertArrayEquals(new byte[4], bus.image(0x0e, 4));
  }
  @Test void allChannelPulseTargetsBroadcastRegisters() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x40);
    Pca9685Device pwm = new Pca9685Device(bus);
    pwm.setAllPWM(0xfff, 0x800);
    assertArrayEquals(new byte[]{(byte) 0xff, 0x0f, 0, 8}, bus.image(0xfa, 4));
  }
  @Test void fullOnAndFullOffPreservePulseCountAndCanBeRemoved() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x40);
    Pca9685Device pwm = new Pca9685Device(bus);
    pwm.setPWM(0, 0x345, 0x678);
    pwm.getLedControlRegisters()[0].setFullOn(true);
    pwm.getLedControlRegisters()[0].setFullOff(true);
    assertArrayEquals(new byte[]{0x45, 0x13, 0x78, 0x16}, bus.image(6, 4));
    pwm.getLedControlRegisters()[0].setFullOn(false);
    pwm.getLedControlRegisters()[0].setFullOff(false);
    assertArrayEquals(new byte[]{0x45, 3, 0x78, 6}, bus.image(6, 4));
  }
  @Test void resetClearsEveryPreviouslyProgrammedChannel() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x40);
    Pca9685Device pwm = new Pca9685Device(bus);
    pwm.setPWM(0, 1, 4095);
    pwm.setPWM(15, 200, 300);
    pwm.reset();
    assertArrayEquals(new byte[64], bus.image(6, 64));
    assertArrayEquals(new byte[4], bus.image(0xfa, 4));
  }
}
