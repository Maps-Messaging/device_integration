package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.i2c.devices.sensors.bmp280.BMP280Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.GasSensorController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedAddressDetectionTest {
  @Test
  void gasProbeRecognizesCoAndOxygenWithoutMounting() {
    GasSensorController controller = new GasSensorController();
    assertTrue(controller.canDetect());
    for (byte[] response : new byte[][] {
        {(byte) 0xff, (byte) 0x86, 0, 0, 4, 0, 0, 0, 0x76},
        {(byte) 0xff, (byte) 0x86, 0, 0, 5, 1, 0, 0, 0x74}}) {
      ProbeDevice device = new ProbeDevice(response);
      assertTrue(controller.detect(device));
      assertEquals(0, device.requestRegister);
      assertArrayEquals(new byte[] {(byte) 0xff, 1, (byte) 0x86, 0, 0, 0, 0, 0, 0x79}, device.request);
      assertEquals(0, device.responseRegister);
    }
  }

  @Test
  void gasProbeRejectsMalformedUnknownAndIncompleteReplies() {
    GasSensorController controller = new GasSensorController();
    for (byte[] response : new byte[][] {
        {0, (byte) 0x86, 0, 0, 4, 0, 0, 0, 0x76},
        {(byte) 0xff, (byte) 0x88, 0, 0, 4, 0, 0, 0, 0x74},
        {(byte) 0xff, (byte) 0x86, 0, 0, 4, 0, 0, 0, 0},
        {(byte) 0xff, (byte) 0x86, 0, 0, 0x7e, 0, 0, 0, (byte) 0xfc},
        {(byte) 0xff, (byte) 0x86, 0, 0, 4, 3, 0, 0, 0x73},
        {(byte) 0xff, (byte) 0x86, 0, 0, 4},
        new byte[9]}) {
      assertFalse(controller.detect(new ProbeDevice(response)));
    }
  }

  @Test
  void gasProbeRejectsFailedWritesAndIoFailures() {
    GasSensorController controller = new GasSensorController();
    assertFalse(controller.detect(new ProbeDevice(new byte[9]) {
      @Override public int writeRegister(int register, byte[] data) { return -1; }
    }));
    assertFalse(controller.detect(new ProbeDevice(new byte[9]) {
      @Override public int readRegister(int register, byte[] data, int offset, int length) {
        throw new Pi4JException("I2C read failed");
      }
    }));
  }

  @Test
  void bmpProbeRequiresBoschChipIdentity() {
    BMP280Controller controller = new BMP280Controller();
    assertTrue(controller.canDetect());
    assertTrue(controller.detect(new ScriptedI2CDevice(0x76) {
      @Override public int readRegister(int register) {
        assertEquals(0xd0, register);
        return 0x58;
      }
    }));
    assertFalse(controller.detect(new ScriptedI2CDevice(0x76)));
    assertFalse(controller.detect(new ScriptedI2CDevice(0x76) {
      @Override public int readRegister(int register) { throw new Pi4JException("I2C read failed"); }
    }));
  }

  private static class ProbeDevice extends ScriptedI2CDevice {
    private int requestRegister = -1;
    private int responseRegister = -1;
    private byte[] request;

    ProbeDevice(byte[] response) {
      super(0x76, response);
    }

    @Override public int writeRegister(int register, byte[] data) {
      requestRegister = register;
      request = data.clone();
      return data.length;
    }

    @Override public int readRegister(int register, byte[] data, int offset, int length) {
      responseRegister = register;
      return super.readRegister(register, data, offset, length);
    }
  }
}
