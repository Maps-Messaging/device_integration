package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ReadMeasurementRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SerialNumberRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetDataReadyFlagCommand;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetDeviceStatusCommand;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetFanCleaningIntervalCommand;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetVersionCommand;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SensorResponseLengthTest {

  @Test
  void scd41RejectsShortHardwareReads() {
    AddressableDevice device = new StubDevice(2);
    assertThrows(IllegalStateException.class, () -> new ReadMeasurementRequest(device).getResponse());
    assertThrows(IllegalStateException.class, () -> new SerialNumberRequest(device).getSerialNumber());
  }

  @Test
  void sen6xRejectsShortHardwareReads() {
    Sen6xCommandHelper helper = new Sen6xCommandHelper(new StubDevice(2));
    assertTimeoutPreemptively(Duration.ofSeconds(1),
        () -> assertThrows(IOException.class, () -> helper.requestResponse(0x0202, 3, 0)));
  }

  @Test
  void sen6xCommandsRejectTruncatedDecodedResponses() {
    Sen6xCommandHelper helper = new Sen6xCommandHelper(new StubDevice(0)) {
      @Override
      public byte[] requestResponse(int command, int expectedResponseLength, int delayMillis) {
        return new byte[1];
      }
    };
    assertThrows(IOException.class, () -> new GetDataReadyFlagCommand(helper).execute());
    assertThrows(IOException.class, () -> new GetDeviceStatusCommand(helper).execute());
    assertThrows(IOException.class, () -> new GetFanCleaningIntervalCommand(helper).execute());
    assertThrows(IOException.class, () -> new GetVersionCommand(helper).execute());
  }

  private static final class StubDevice implements AddressableDevice {
    private final int readLength;

    private StubDevice(int readLength) {
      this.readLength = readLength;
    }

    @Override public void close() { }
    @Override public int getBus() { return 1; }
    @Override public int write(int val) { return 1; }
    @Override public int write(byte[] buffer, int offset, int length) { return length; }
    @Override public int writeRegister(int register, byte[] data) { return data.length; }
    @Override public int read(byte[] buffer, int offset, int length) { return readLength; }
    @Override public int readRegister(int register) { return 0; }
    @Override public int readRegister(int register, byte[] buffer, int offset, int length) { return readLength; }
    @Override public int getDevice() { return 0x62; }
    @Override public int read() { return 0; }
  }
}
