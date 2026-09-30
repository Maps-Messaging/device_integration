package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.Ds3231Rtc;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BusDeviceRtcTest {
  @Test void dateDecodesBcdLeapDayAndReloadsNewDate() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x68);
    bus.seed(4, 0x29, 0x02, 0x24);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertEquals(LocalDate.of(2024, 2, 29), rtc.getDate());
    bus.seed(4, 0x31, 0x12, 0x79);
    assertEquals(LocalDate.of(2079, 12, 31), rtc.getDate());
  }
  @Test void dateWriteEncodesDecimalDateWithoutChangingClockRegisters() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x68);
    bus.seed(0, 0x58, 0x47, 0x09);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    rtc.setDate(LocalDate.of(2037, 11, 28));
    assertArrayEquals(new byte[]{0x28, 0x11, 0x37}, bus.image(4, 3));
    assertArrayEquals(new byte[]{0x58, 0x47, 9}, bus.image(0, 3));
  }
  @Test void minutesAndSecondsDecodeAndEncodeDecimalBoundaries() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x68);
    bus.seed(0, 0x59, 0x48);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertEquals(59, rtc.getSecondsRegister().getSeconds());
    assertEquals(48, rtc.getMinutesRegister().getMinutes());
    rtc.getSecondsRegister().setSeconds(12);
    rtc.getMinutesRegister().setMinutes(37);
    assertArrayEquals(new byte[]{0x12, 0x37}, bus.image(0, 2));
  }
  @Test void positiveTemperatureIncludesQuarterDegreeFractionAndRefreshes() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x68);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    bus.seed(0x11, 25, 0x40);
    assertEquals(25.25f, rtc.getTemperatureRegister().getTemperature());
    bus.seed(0x11, 0, 0xc0);
    assertEquals(0.75f, rtc.getTemperatureRegister().getTemperature());
  }
}
