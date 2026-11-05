package io.mapsmessaging.devices.i2c.devices.sensors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.AS3935Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.AS3935Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.AfeRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.LightningStrikeRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.LightningRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.ThresholdRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers.TunCapRegister;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.NumericSensorReading;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class As3935PollingTest {
  @Test
  void directSensorReadingsRefreshWithoutAControllerCollection() throws Exception {
    Hardware hardware = new Hardware(3);
    AS3935Sensor sensor = (AS3935Sensor) new AS3935Controller().mount(hardware).getDevice();
    hardware.event(8, 5, 0, 0, 12);
    assertEquals(5, sensor.getReadings().getFirst().getValue().getResult());
    hardware.event(8, 7, 0, 0, 14);
    assertEquals(7, sensor.getReadings().getFirst().getValue().getResult());
    hardware.shortPoll = true;
    assertTrue(sensor.getReadings().getFirst().getValue().hasError());
    hardware.shortPoll = false;
    assertEquals(7, sensor.getReadings().getFirst().getValue().getResult());
  }

  @Test
  void unmountedControllerRetainsEmptyStateBehaviour() throws Exception {
    assertEquals(0, json(new AS3935Controller()).size());
  }

  @Test
  void configurationReadsAndWritesUseCurrentHardwareFields() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(1, 0x22).put(2, 0xc2).put(8, 0);
    ThresholdRegister threshold = new ThresholdRegister(device.bus());
    LightningRegister lightning = new LightningRegister(device.bus());
    TunCapRegister tuning = new TunCapRegister(device.bus());
    device.put(1, 0x69).put(2, 0x95).put(8, 0xec);
    assertEquals(6, threshold.getNoiseFloorLevel());
    assertEquals(9, threshold.getWatchdogThreshold());
    assertEquals(5, lightning.getSpikeRejection());
    assertEquals(1, lightning.getMinNumLightning());
    assertEquals(12, tuning.getTuningCap());
    assertTrue(tuning.isDispTRCOEnabled());
    assertTrue(tuning.isDispSRCOEnabled());
    device.put(1, 0x72).put(2, 0xe3).put(8, 0x8b);
    threshold.setWatchdogThreshold(4);
    lightning.setSpikeRejection(7);
    tuning.setTuningCap(12);
    assertEquals(0x74, device.value(1));
    assertEquals(0xe7, device.value(2));
    assertEquals(0x8c, device.value(8));
  }

  @Test
  void powerCycleRecalibratesWithoutLeavingPowerDownSet() throws Exception {
    Hardware hardware = new Hardware(3);
    AS3935Sensor sensor = (AS3935Sensor) new AS3935Controller().mount(hardware).getDevice();
    sensor.powerOff();
    assertEquals(1, hardware.image[0] & 1);
    sensor.powerOn();
    assertEquals(0, hardware.image[0] & 1);
    assertEquals(12, hardware.image[8] & 0x0f);
    assertEquals(0, hardware.image[8] & 0xe0);
  }

  @Test
  void defaultErrorHandlingDoesNotPublishStaleNumbersAfterShortPoll() throws Exception {
    Hardware hardware = new Hardware(3);
    I2CDeviceController controller = new AS3935Controller().mount(hardware);
    hardware.event(8, 5, 0, 0, 12);
    json(controller);
    hardware.shortPoll = true;
    hardware.reads.clear();
    JsonObject failed = json(controller);
    assertTrue(failed.get("lightning_energy").getAsJsonPrimitive().isString());
    assertTrue(failed.get("lightning_distance").getAsJsonPrimitive().isString());
    assertTrue(failed.get("interrupt_reason").getAsJsonPrimitive().isString());
    assertEquals(List.of(3), hardware.reads);
  }

  @Test
  void rawEnergyUsesLowMiddleHighOrderAndAllTwentyOneBits() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(4, 0x56, 0x34, 0xf2);
    LightningStrikeRegister register = new LightningStrikeRegister(device.bus());
    assertEquals(0x123456, register.getEnergy());
    device.put(4, 0xff, 0xff, 0xff);
    assertEquals(0x1fffff, register.getEnergy());
  }

  @Test
  void oscillatorSelectionUsesDocumentedBitsAndPreservesOtherFields() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(8, 0x8c);
    TunCapRegister register = new TunCapRegister(device.bus());
    register.setDispTRCOEnabled(true);
    assertEquals(0xac, device.value(8));
    register.setDispSRCOEnabled(true);
    assertEquals(0xec, device.value(8));
    register.setDispTRCOEnabled(false);
    assertEquals(0xcc, device.value(8));
  }

  @Test
  void configurationUpdatesRefreshAfterHardwareResetOrPowerChange() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice().put(0, 0x24);
    AfeRegister register = new AfeRegister(device.bus());
    register.setPowerDown(true);
    register.setOutdoor();
    assertEquals(0x1d, device.value(0));
    device.put(0, 0xa5);
    register.setPowerDown(false);
    assertEquals(0xa4, device.value(0));
  }

  @Test
  void discoverySupportsAllBoardAddressesWithoutConfiguringHardware() {
    AS3935Controller controller = new AS3935Controller();
    assertTrue(controller.canDetect());
    assertArrayEquals(new int[]{1, 2, 3}, controller.getAddressRange());
    for (int address : new int[]{1, 2, 3}) {
      Hardware hardware = new Hardware(address);
      assertTrue(controller.detect(hardware));
      assertEquals(0, hardware.writes.size());
      hardware.image[0] = (byte) 0xff;
      assertFalse(controller.detect(hardware));
    }
  }

  @Test
  void mountingSelectsBoardTuningCalibratesAndChecksLiveConnection() throws Exception {
    Hardware hardware = new Hardware(3);
    I2CDeviceController controller = new AS3935Controller().mount(hardware);
    AS3935Sensor sensor = (AS3935Sensor) controller.getDevice();
    assertEquals(3, controller.getMountedAddress());
    assertEquals(12, hardware.image[8] & 0x0f);
    assertEquals(0, hardware.image[8] & 0xe0);
    assertTrue(hardware.writes.stream().anyMatch(write -> Arrays.equals(write, new byte[]{8, 0x4c})));
    assertTrue(sensor.isConnected());
    hardware.failedRegister = 0;
    assertFalse(sensor.isConnected());
    hardware.failedRegister = -1;
    assertTrue(sensor.isConnected());
    NumericSensorReading<?> energy = (NumericSensorReading<?>) sensor.getReadings().getFirst();
    assertEquals(0x1fffff, energy.getMaximum());
  }

  @Test
  void failedOscillatorCalibrationPreventsMounting() {
    Hardware hardware = new Hardware(3);
    hardware.image[0x3a] = 0x40;
    assertThrows(IOException.class, () -> new AS3935Controller().mount(hardware));
  }

  @Test
  void jsonUsesOneRegisterWindowForLightningAndNormalIdleStates() throws Exception {
    Hardware hardware = new Hardware(3);
    I2CDeviceController controller = new AS3935Controller().mount(hardware);
    hardware.event(8, 0x56, 0x34, 0x12, 20);
    hardware.reads.clear();
    JsonObject lightning = json(controller);
    assertEquals(0x123456, lightning.get("lightning_energy").getAsInt());
    assertEquals(20, lightning.get("lightning_distance").getAsInt());
    assertEquals("Lightning interrupt", lightning.get("interrupt_reason").getAsString());
    assertEquals(List.of(3), hardware.reads);
    for (int reason : new int[]{0, 1, 4}) {
      hardware.event(reason, 0x56, 0x34, 0x12, 20);
      JsonObject idle = json(controller);
      assertEquals(0, idle.get("lightning_energy").getAsInt());
      assertEquals(-1, idle.get("lightning_distance").getAsInt());
    }
    hardware.event(8, 1, 0, 0, 63);
    assertEquals(-1, json(controller).get("lightning_distance").getAsInt());
  }

  @Test
  void failedPollDoesNotReusePreviousStrikeAndNextPollRecovers() throws Exception {
    Hardware hardware = new Hardware(3);
    I2CDeviceController controller = new AS3935Controller().mount(hardware);
    hardware.event(8, 5, 0, 0, 12);
    assertEquals(5, json(controller).get("lightning_energy").getAsInt());
    controller.setRaiseExceptionOnError(true);
    hardware.shortPoll = true;
    assertFalse(json(controller).has("lightning_energy"));
    hardware.shortPoll = false;
    hardware.event(8, 7, 0, 0, 14);
    assertEquals(7, json(controller).get("lightning_energy").getAsInt());
  }

  private static JsonObject json(I2CDeviceController controller) throws IOException {
    return JsonParser.parseString(new String(controller.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
  }

  private static final class Hardware implements AddressableDevice {
    private final byte[] image = new byte[256];
    private final List<byte[]> writes = new ArrayList<>();
    private final List<Integer> reads = new ArrayList<>();
    private final int address;
    private int failedRegister = -1;
    private boolean shortPoll;

    Hardware(int address) {
      this.address = address;
      image[0] = 0x24;
      image[1] = 0x22;
      image[2] = (byte) 0xc2;
      image[0x3a] = (byte) 0x80;
      image[0x3b] = (byte) 0x80;
    }

    void event(int... values) {
      for (int index = 0; index < values.length; index++) image[3 + index] = (byte) values[index];
    }

    @Override public void close() { }
    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return address; }
    @Override public int write(int value) { throw new AssertionError("Expected register I/O"); }
    @Override public int write(byte[] data, int offset, int length) { throw new AssertionError("Expected register I/O"); }
    @Override public int writeRegister(int register, byte[] data) {
      writes.add(new byte[]{(byte) register, data[0]});
      if (register == 0x3c && data[0] == (byte) 0x96) {
        Arrays.fill(image, 0, 9, (byte) 0);
        image[0] = 0x24;
        image[1] = 0x22;
        image[2] = (byte) 0xc2;
      } else {
        System.arraycopy(data, 0, image, register, data.length);
      }
      return data.length;
    }
    @Override public int readRegister(int register) {
      reads.add(register);
      return register == failedRegister ? -1 : image[register] & 0xff;
    }
    @Override public int readRegister(int register, byte[] data, int offset, int length) {
      reads.add(register);
      if (register == failedRegister) return -1;
      System.arraycopy(image, register, data, offset, length);
      return shortPoll && register == 3 ? length - 1 : length;
    }
    @Override public int read(byte[] data, int offset, int length) { throw new AssertionError("Expected register I/O"); }
    @Override public int read() { throw new AssertionError("Expected register I/O"); }
  }
}
