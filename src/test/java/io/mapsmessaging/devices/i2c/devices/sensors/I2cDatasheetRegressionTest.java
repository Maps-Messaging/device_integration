package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.mapsmessaging.devices.i2c.devices.sensors.am2320.AM2320Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ForcedRecalibrationRequest;
import org.junit.jupiter.api.Test;

class I2cDatasheetRegressionTest {

  @Test
  void scd41ForcedRecalibrationUsesReferenceWordAndReturnsCorrection() {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x62, new byte[] {0x7f, (byte) 0xce, 0x7b});
    ForcedRecalibrationRequest request = new ForcedRecalibrationRequest(device);

    assertEquals(-50, request.recalibrate(480));
    assertArrayEquals(new byte[] {0x36, 0x2f, 0x01, (byte) 0xe0, (byte) 0xb4}, device.writes().getFirst());
  }

  @Test
  void scd41RejectsInvalidReferenceWithoutWriting() {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x62);
    ForcedRecalibrationRequest request = new ForcedRecalibrationRequest(device);

    assertThrows(IllegalArgumentException.class, () -> request.recalibrate(0));
    assertEquals(0, device.writes().size());
  }

  @Test
  void scd41RejectsFailedRecalibration() {
    byte[] failure = {(byte) 0xff, (byte) 0xff, crc8((byte) 0xff, (byte) 0xff)};
    ForcedRecalibrationRequest request = new ForcedRecalibrationRequest(new ScriptedI2CDevice(0x62, failure));

    assertThrows(IllegalStateException.class, () -> request.recalibrate(480));
  }

  @Test
  void am2320DecodesSignMagnitudeNegativeTemperature() throws Exception {
    byte[] frame = {3, 4, 0x01, (byte) 0xf4, (byte) 0x80, 0x64, 0, 0};
    int crc = crc16(frame, 6);
    frame[6] = (byte) crc;
    frame[7] = (byte) (crc >>> 8);
    AM2320Sensor sensor = new AM2320Sensor(new ScriptedI2CDevice(0x5c, frame));

    assertEquals(-10.0f, sensor.getTemperature(), 0.01f);
    assertEquals(50.0f, sensor.getHumidity(), 0.01f);
  }

  private static byte crc8(byte first, byte second) {
    int crc = 0xff;
    for (byte value : new byte[] {first, second}) {
      crc ^= value & 0xff;
      for (int i = 0; i < 8; i++) {
        crc = (crc & 0x80) != 0 ? ((crc << 1) ^ 0x31) & 0xff : (crc << 1) & 0xff;
      }
    }
    return (byte) crc;
  }

  private static int crc16(byte[] frame, int length) {
    int crc = 0xffff;
    for (int i = 0; i < length; i++) {
      crc ^= frame[i] & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : crc >>> 1;
      }
    }
    return crc & 0xffff;
  }
}
