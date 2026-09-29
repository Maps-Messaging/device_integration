package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.am2320.AM2320Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.am2320.AM2320Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i.Pmsa003iController;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetDataReadyFlagCommand;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetFanCleaningIntervalCommand;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Sensor;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ScriptedSensorTest {
  @Test
  void bh1750MountsAndReadsLuxFromScriptedBus() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x23, new byte[] {0x01, 0x2c});
    I2CDeviceController controller = new Bh1750Controller().mount(bus);
    try {
      assertEquals(0x23, controller.getMountedAddress());
      assertTrue(controller.detect(bus));
      assertFalse(bus.writes().isEmpty());
      String state = new String(controller.getDeviceState());
      assertTrue(JsonParser.parseString(state).getAsJsonObject().has("lux"), state);
    } finally {
      controller.close();
    }
  }

  @Test
  void am2320MountsAndDecodesHumidityAndTemperature() throws Exception {
    byte[] response = {3, 4, 2, 43, 0, (byte) 235, 0, 0};
    int checksum = crc16(response, 6);
    response[6] = (byte) checksum;
    response[7] = (byte) (checksum >>> 8);
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x5c, response);
    I2CDeviceController controller = new AM2320Controller().mount(bus);
    try {
      AM2320Sensor sensor = (AM2320Sensor) controller.getDevice();
      assertEquals(23.5f, sensor.getTemperature(), 0.01f);
      assertEquals(55.5f, sensor.getHumidity(), 0.01f);
      assertTrue(bus.writes().stream().anyMatch(data -> Arrays.equals(data, new byte[] {3, 0, 4})));
    } finally {
      controller.close();
    }
  }

  @Test
  void am2320RejectsCorruptChecksumEvenWithValidHeader() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x5c,
        new byte[] {3, 4, 2, 43, 0, (byte) 235, 0, 0});
    I2CDeviceController controller = new AM2320Controller().mount(bus);
    try {
      AM2320Sensor sensor = (AM2320Sensor) controller.getDevice();
      assertEquals(-273.0f, sensor.getTemperature());
      assertEquals(-273.0f, sensor.getHumidity());
    } finally {
      controller.close();
    }
  }

  @Test
  void sen6xReadsScriptedReadyFlagAndInterval() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b,
        new byte[] {0, 1, crc((byte) 0, (byte) 1)},
        new byte[] {0x12, 0x34, crc((byte) 0x12, (byte) 0x34)});
    Sen6xCommandHelper helper = new Sen6xCommandHelper(bus);
    assertTrue(new GetDataReadyFlagCommand(helper).execute());
    assertEquals(0x1234, new GetFanCleaningIntervalCommand(helper).execute());
    assertArrayEquals(new byte[] {0x02, 0x02}, bus.writes().get(0));
    assertArrayEquals(new byte[] {(byte) 0xd2, 0x10}, bus.writes().get(1));
  }

  @Test
  void pmsa003iReadsParticulateCountsFromScriptedRegisterBlock() throws Exception {
    byte[] frame = new byte[32];
    frame[4] = 0;
    frame[5] = 12;
    frame[6] = 0;
    frame[7] = 25;
    frame[8] = 0;
    frame[9] = 40;
    frame[28] = 3;
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x12, frame);
    I2CDeviceController controller = new Pmsa003iController().mount(bus);
    try {
      var state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();
      assertEquals(12, state.get("pm_1_0").getAsInt());
      assertEquals(25, state.get("pm_2_5").getAsInt());
      assertEquals(40, state.get("pm_10").getAsInt());
      assertEquals(3, JsonParser.parseString(new String(controller.getDeviceConfiguration()))
          .getAsJsonObject().get("version").getAsInt());
    } finally {
      controller.close();
    }
  }

  @Test
  void sht31ReadsTemperatureAndHumidityWithValidCrc() throws Exception {
    byte[] frame = {0x60, 0, crc((byte) 0x60, (byte) 0),
        (byte) 0x80, 0, crc((byte) 0x80, (byte) 0)};
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44, frame);
    I2CDeviceController controller = new Sht31Controller().mount(bus);
    try {
      Sht31Sensor sensor = (Sht31Sensor) controller.getDevice();
      assertEquals(20.6f, sensor.getTemperature(), 0.2f);
      assertEquals(50.0f, sensor.getHumidity(), 0.2f);
      assertArrayEquals(new byte[] {0x30, (byte) 0xa2}, bus.writes().get(0));
      assertArrayEquals(new byte[] {(byte) 0xe0, 0}, bus.writes().get(2));
    } finally {
      controller.close();
    }
  }

  @Test
  void sht31RejectsCorruptMeasurementCrc() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44,
        new byte[] {0x60, 0, 0, (byte) 0x80, 0, 0});
    I2CDeviceController controller = new Sht31Controller().mount(bus);
    try {
      Sht31Sensor sensor = (Sht31Sensor) controller.getDevice();
      assertThrows(IllegalStateException.class, sensor::getTemperature);
    } finally {
      controller.close();
    }
  }

  private static byte crc(byte first, byte second) {
    int crc = 0xff;
    for (byte value : new byte[] {first, second}) {
      crc ^= value & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        crc = (crc & 0x80) != 0 ? ((crc << 1) ^ 0x31) & 0xff : (crc << 1) & 0xff;
      }
    }
    return (byte) crc;
  }

  private static int crc16(byte[] data, int length) {
    int crc = 0xffff;
    for (int index = 0; index < length; index++) {
      crc ^= data[index] & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : crc >>> 1;
      }
    }
    return crc & 0xffff;
  }
}
