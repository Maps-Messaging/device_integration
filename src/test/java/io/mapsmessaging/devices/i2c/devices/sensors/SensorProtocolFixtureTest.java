package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;

import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands.*;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i.Pmsa003iController;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Sensor;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

class SensorProtocolFixtureTest {
  @TestFactory
  Stream<DynamicTest> sht31PeriodicCommandsMatchRepeatabilityAndRate() {
    // Literal command table from SHT3x specification, high/medium/low order.
    int[][] commands = {{0x2032,0x2024,0x202f}, {0x2130,0x2126,0x212d},
        {0x2236,0x2220,0x222b}, {0x2334,0x2322,0x2329}, {0x2737,0x2721,0x272a}};
    List<DynamicTest> tests = new ArrayList<>();
    for (int i = 0; i < 5; i++) for (int j = 0; j < 3; j++) {
      Mps rate = Mps.values()[i]; Repeatability repeatability = Repeatability.values()[j]; int expected = commands[i][j];
      tests.add(DynamicTest.dynamicTest(rate + " " + repeatability, () -> {
        var bus = new ScriptedI2CDevice(0x44);
        new PeriodicReadCommand(repeatability, rate).sendCommand(null, bus);
        assertArrayEquals(new byte[] {(byte) (expected >>> 8), (byte) expected}, bus.writes().get(0));
      }));
    }
    return tests.stream();
  }

  @Test
  void sht31HeaterAndStatusCommandsUseWireProtocol() {
    var bus = new ScriptedI2CDevice(0x44, frame(0x8010));
    new HeaterControlCommand(true).sendCommand(null, bus);
    new HeaterControlCommand(false).sendCommand(null, bus);
    assertArrayEquals(frame(0x8010), new StatusRegisterCommand().sendCommand(null, bus));
    new ClearStatusRegisterCommand().sendCommand(null, bus);
    new BreakCommand().sendCommand(null, bus);
    new ArtCommand().sendCommand(null, bus);
    assertEquals(List.of("306d", "3066", "f32d", "3041", "3093", "2b32"),
        bus.writes().stream().map(bytes -> HexFormat.of().formatHex(bytes)).toList());
  }

  @TestFactory
  Stream<DynamicTest> sht31RawEndpointMeasurementsConvertAndShareOneRead() {
    return Stream.of(new float[] {0,0,-45,0}, new float[] {65535,65535,130,100}, new float[] {32768,32768,42.5013f,50.0008f})
        .map(vector -> DynamicTest.dynamicTest("raw=" + vector[0], () -> {
          var bus = new ScriptedI2CDevice(0x44, frame((int) vector[0], (int) vector[1]));
          var sensor = new Sht31Sensor(bus) { @Override public void delay(int millis) { } };
          try {
            assertEquals(vector[2], sensor.getTemperature(), 0.001f);
            assertEquals(vector[3], sensor.getHumidity(), 0.001f);
            assertEquals(3, bus.writes().size(), "Temperature/humidity must share the sample");
          } finally { sensor.close(); }
        }));
  }

  @Test
  void sht31SecondWordCrcFailureIsRejected() {
    byte[] raw = frame(32768, 32768); raw[5] ^= 1;
    assertThrows(IllegalStateException.class, () -> new Command(0xe000, 0, 6) { }.sendCommand(null, new ScriptedI2CDevice(0x44, raw)));
  }

  @Test
  void sht31PowerTransitionsEmitResetAndRestartPeriodicSampling() throws Exception {
    var bus = new ScriptedI2CDevice(0x44);
    var sensor = new Sht31Sensor(bus) { @Override public void delay(int millis) { } };
    try {
      sensor.powerOff();
      sensor.powerOn();
      assertEquals(List.of("30a2", "2126", "30a2", "30a2", "2126"),
          bus.writes().stream().map(bytes -> HexFormat.of().formatHex(bytes)).toList());
    } finally { sensor.close(); }
  }

  @Test
  void particulateFullFrameExportsEveryFieldAndAqi() throws Exception {
    byte[] raw = new byte[32]; raw[0]=0x42; raw[1]=0x4d; raw[3]=28;
    int[] words = {10,12,54,11,13,55,1000,500,250,100,50,25};
    for (int i=0;i<words.length;i++) { raw[4+i*2]=(byte)(words[i]>>>8); raw[5+i*2]=(byte)words[i]; }
    raw[28]=3; int checksum=0; for(int i=0;i<30;i++) checksum+=raw[i]&0xff;
    raw[30]=(byte)(checksum>>>8);raw[31]=(byte)checksum;
    var bus = new ScriptedI2CDevice(0x12, raw);
    var controller = new Pmsa003iController().mount(bus);
    try {
      var state = JsonParser.parseString(new String(controller.getDeviceState())).getAsJsonObject();
      String[] names={"pm_1_0","pm_2_5","pm_10","pm_1_0_atm","pm_2_5_atm","pm_10_atm","particles_gt_3","particles_gt_5","particles_gt_10","particles_gt_25","particles_gt_50","particles_gt_100"};
      for(int i=0;i<words.length;i++) assertEquals(words[i],state.get(names[i]).getAsInt(),names[i]);
      assertEquals(50,state.get("AQI").getAsInt());
      assertEquals("Good",state.get("AQICategory").getAsString());
      var config=JsonParser.parseString(new String(controller.getDeviceConfiguration())).getAsJsonObject();
      assertEquals(3,config.get("version").getAsInt());
    } finally { controller.close(); }
  }

  @Test
  void bh1750UsesBigEndianCountsAndOnePointTwoLuxScale() throws Exception {
    var bus = new ScriptedI2CDevice(0x23,new byte[]{1,44});
    var sensor = new Bh1750Sensor(bus);
    try { assertEquals(250f,((Number) sensor.getReadings().stream().filter(r -> r.getName().equals("lux")).findFirst().orElseThrow().getValue().getResult()).floatValue(),0.01f); }
    finally { sensor.close(); }
  }
}
