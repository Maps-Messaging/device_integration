package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen66Sensor;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

class Sen6xLifecycleTest {
  @Test
  void failedResetIsReported() {
    ScriptedI2CDevice delegate = Sen6xCapabilitiesTest.bus("SEN66");
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] buffer, int offset, int length) {
        return delegate.read(buffer, offset, length);
      }
      @Override
      public int write(byte[] buffer, int offset, int length) {
        return buffer[offset] == (byte) 0xd3 ? -1 : length;
      }
    };
    assertThrows(UncheckedIOException.class, () -> new Sen66Sensor(device));
  }

  @Test
  void lifecycleDoesNotHideFailedCommands() throws Exception {
    ScriptedI2CDevice delegate = Sen6xCapabilitiesTest.bus("SEN66");
    boolean[] failed = {false};
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] buffer, int offset, int length) {
        return delegate.read(buffer, offset, length);
      }
      @Override
      public int write(byte[] buffer, int offset, int length) {
        return failed[0] ? -1 : length;
      }
    };
    Sen66Sensor sensor = new Sen66Sensor(device);
    failed[0] = true;
    assertThrows(UncheckedIOException.class, sensor::powerOff);
    assertThrows(IOException.class, sensor::reset);
  }
  @Test
  void recoveryDoesNotRestartAnIntentionallyStoppedSensor() throws Exception {
    Sen66Sensor sensor = new Sen66Sensor(Sen6xCapabilitiesTest.bus("SEN66"));
    sensor.powerOff();
    var manager = new io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen66MeasurementManager(sensor.getHelper()) {
      @Override
      public synchronized boolean hasLocked() { return true; }
    };
    new io.mapsmessaging.devices.i2c.devices.sensors.sen6x.ResetMonitor(manager, sensor).check();
    assertFalse(sensor.isMeasuring());
  }

}
