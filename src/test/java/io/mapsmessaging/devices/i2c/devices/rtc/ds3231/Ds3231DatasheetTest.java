package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mapsmessaging.devices.i2c.RegisterBackedI2cDevice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class Ds3231DatasheetTest {

  @Test
  void oscillatorEnabledClearsEoscBit() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x68);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);

    rtc.getControlRegister().setOscillatorEnabled(false);
    assertEquals(0x80, bus.register(0x0e) & 0x80);
    assertFalse(rtc.getControlRegister().isOscillatorEnabled());
    rtc.getControlRegister().setOscillatorEnabled(true);
    assertEquals(0, bus.register(0x0e) & 0x80);
    assertTrue(rtc.getControlRegister().isOscillatorEnabled());
  }

  @Test
  void signedQuarterDegreeTemperaturesAreDecoded() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x68);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    bus.setRegister(0x11, 0xff);
    bus.setRegister(0x12, 0xc0);

    assertEquals(-0.25f, rtc.getTemperatureRegister().getTemperature(), 0.0001f);
  }

  @Test
  void dailyAlarmsMaskDayAndKeepDateSelection() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x68);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);

    rtc.setAlarm(1, LocalTime.of(12, 34, 56));
    assertEquals(0x80, bus.register(0x0a) & 0xc0);
    assertEquals(0x00, bus.register(0x0a) & 0x40);
    rtc.setAlarm(2, LocalTime.of(15, 45));
    assertEquals(0x80, bus.register(0x0d) & 0xc0);
    rtc.setAlarm(1, LocalDateTime.of(2026, 11, 6, 12, 34, 56));
    assertEquals(0, bus.register(0x0a) & 0xc0);
    assertEquals(6, rtc.getAlarm1DayRegister().getDay());
  }

  @Test
  void dateRegistersSupportCenturyAndLateDecades() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x68);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);

    rtc.setDate(LocalDate.of(2199, 12, 31));
    assertEquals(0x80 | 0x12, bus.register(0x05));
    assertEquals(0x99, bus.register(0x06));
    assertEquals(LocalDate.of(2199, 12, 31), rtc.getDate());
  }

  @Test
  void timestampUsesContiguousRegisterSnapshot() throws Exception {
    RegisterBackedI2cDevice bus = new RegisterBackedI2cDevice(0x68);
    int[] registers = {0x59, 0x59, 0x23, 0x05, 0x31, 0x12, 0x99};
    for (int index = 0; index < registers.length; index++) {
      bus.setRegister(index, registers[index]);
    }
    Ds3231Rtc rtc = new Ds3231Rtc(bus);

    assertEquals(LocalDateTime.of(2099, 12, 31, 23, 59, 59), rtc.getDateTime());
  }
}
