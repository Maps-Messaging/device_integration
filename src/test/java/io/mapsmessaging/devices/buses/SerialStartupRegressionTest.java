package io.mapsmessaging.devices.buses;

import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.devices.serial.devices.sensors.sen0640.Sen0640Sensor;
import io.mapsmessaging.devices.serial.devices.sensors.sen0642.Sen0642Sensor;
import io.mapsmessaging.devices.serial.devices.sensors.sen0657.Sen0657Sensor;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerialStartupRegressionTest {
  @Test
  void fragmentedSolarResponseProducesFirstMeasurement() throws Exception {
    assertEquals(700, new Sen0640Sensor(new FragmentPort(700)).getSolarRadiation());
  }

  @Test
  void fragmentedUvResponseProducesFirstMeasurement() throws Exception {
    assertEquals(1.25f, new Sen0642Sensor(new FragmentPort(125)).getUvIrradiance());
  }

  @Test
  void fragmentedWeatherResponsesProduceCompleteFirstSnapshot() throws Exception {
    Sen0657Sensor sensor = new Sen0657Sensor(new FragmentPort(550, 225, 1013, 0, 12000, 340, 0, 2, 90, 15));
    assertEquals(1013f, sensor.getAtmosphericPressureHpa());
    assertEquals(22.5f, sensor.getTemperatureCelsius());
    assertEquals(12000L, sensor.getLightIntensityLux());
    assertEquals(3.4f, sensor.getWindSpeedMetersPerSecond());
    assertEquals(1.5f, sensor.getRainfallMillimeters());
  }

  @Test
  void failedInitialWeatherGroupCannotPublishZeroAndRetryHasNoFailedCache() throws Exception {
    FragmentPort port = new FragmentPort();
    port.fail = true;
    Sen0657Sensor sensor = new Sen0657Sensor(port);
    assertThrows(UncheckedIOException.class, sensor::getAtmosphericPressureHpa);
    port.fail = false;
    port.values = new int[]{550, 225, 1013, 0, 12000, 340, 0, 2, 90, 15};
    assertEquals(1013f, sensor.getAtmosphericPressureHpa());
  }

  @Test
  void failureAfterOneWeatherGroupDoesNotCommitPartialSnapshot() throws Exception {
    FragmentPort port = new FragmentPort(550, 225);
    Sen0657Sensor sensor = new Sen0657Sensor(port);
    assertThrows(UncheckedIOException.class, sensor::getTemperatureCelsius);
    assertThrows(UncheckedIOException.class, sensor::getHumidityRelativePercent);
  }

  private static final class FragmentPort implements SerialDevice {
    private boolean open;
    private boolean fail;
    private int[] values;
    private int valueIndex;
    private byte[] response;
    private int responseIndex;

    private FragmentPort(int... values) {
      this.values = values;
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public boolean openPort() {
      open = true;
      return true;
    }

    @Override
    public String getSystemPortName() {
      return "fragmented-test";
    }

    @Override
    public void closePort() {
      open = false;
    }

    @Override
    public int writeBytes(byte[] request, int length) {
      int count = request[5] & 0xff;
      if (valueIndex + count > values.length) {
        fail = true;
        return length;
      }
      response = new byte[5 + count * 2];
      response[0] = request[0];
      response[1] = 3;
      response[2] = (byte) (count * 2);
      for (int index = 0; index < count; index++) {
        int value = values[valueIndex++];
        response[3 + index * 2] = (byte) (value >> 8);
        response[4 + index * 2] = (byte) value;
      }
      int crc = 0xffff;
      for (int index = 0; index < response.length - 2; index++) {
        crc ^= response[index] & 0xff;
        for (int bit = 0; bit < 8; bit++) {
          crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : crc >>> 1;
        }
      }
      response[response.length - 2] = (byte) crc;
      response[response.length - 1] = (byte) (crc >> 8);
      responseIndex = 0;
      return length;
    }

    @Override
    public int readBytes(byte[] buffer, int remaining) {
      if (fail) {
        return -1;
      }
      int count = Math.min(2, Math.min(remaining, response.length - responseIndex));
      System.arraycopy(response, responseIndex, buffer, 0, count);
      responseIndex += count;
      return count;
    }
  }
}
