package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.Ds3231Rtc;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RtcStartupRegressionTest {

  @Test
  void stoppedOscillatorDoesNotProduceValidTimestamp() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(0x0f, 0x80);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertThrows(IOException.class, rtc::getDateTime);
  }

  @Test
  void invalidCalendarRetainsDateExceptionRatherThanHostFallback() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(4, 0x31, 0x02, 0x24);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    IOException exception = assertThrows(IOException.class, rtc::getDateTime);
    assertInstanceOf(DateTimeException.class, exception.getCause());
  }

  @Test
  void invalidTimeRetainsTimeExceptionRatherThanHostFallback() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(0, 0x70);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    IOException exception = assertThrows(IOException.class, rtc::getDateTime);
    assertInstanceOf(DateTimeException.class, exception.getCause());
  }

  @Test
  void settingCompleteTimestampClearsOscillatorStopFlag() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(0x0f, 0x89);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    rtc.setDateTime(LocalDateTime.of(2026, 10, 21, 9, 15, 30));
    assertFalse(rtc.getStatusRegister().isOscillatorStopped());
    assertEquals(9, bus.readRegister(0x0f));
  }

  @Test
  void firstSensorTemperatureWaitsForConfirmedConversionAndOnlyConvertsOnce() throws Exception {
    BusRegisterDevice bus = clockBus();
    boolean[] conversionRequested = {false};
    int[] conversionPolls = {0};
    AddressableDevice device = (AddressableDevice) Proxy.newProxyInstance(
        AddressableDevice.class.getClassLoader(), new Class<?>[]{AddressableDevice.class}, (proxy, method, args) -> {
          if (method.getName().equals("writeRegister") && ((Integer) args[0]) == 0x0e && ((((byte[]) args[1])[0] & 0x20) != 0)) {
            conversionRequested[0] = true;
          }
          if (method.getName().equals("readRegister") && args.length == 1 && ((Integer) args[0]) == 0x0e && conversionRequested[0]) {
            conversionPolls[0]++;
            if (conversionPolls[0] == 2) {
              bus.seed(0x0e, 0);
              bus.seed(0x11, 23, 0x40);
            }
          }
          return method.invoke(bus, args);
        });
    Ds3231Rtc rtc = new Ds3231Rtc(device);
    assertEquals(23.25f, rtc.getReadings().get(0).getSupplier().get());
    assertEquals(23.25f, rtc.getReadings().get(0).getSupplier().get());
    assertEquals(1, bus.writes.stream().filter(frame -> frame[0] == 0x0e && (frame[1] & 0x20) != 0).count());
  }

  @Test
  void firstSensorTemperatureTimesOutInsteadOfPublishingPowerOnZero() throws Exception {
    BusRegisterDevice bus = clockBus();
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertThrows(IOException.class, () -> rtc.getReadings().get(0).getSupplier().get());
  }

  @Test
  void twentyFourHourTimeIncludesTwentyHourBit() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(2, 0x23);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertEquals(23, rtc.getDateTime().getHour());
  }

  @Test
  void synchronizationEncodesMidnightAsValidTwentyFourHourTime() throws Exception {
    BusRegisterDevice bus = clockBus();
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    rtc.setDateTime(LocalDateTime.of(2026, 10, 21, 0, 15, 30));
    assertEquals(0, bus.readRegister(2));
    assertEquals(0, rtc.getDateTime().getHour());
  }

  @Test
  void invalidTwentyFourHourValueDoesNotAliasToValidHour() throws Exception {
    BusRegisterDevice bus = clockBus();
    bus.seed(2, 0x29);
    Ds3231Rtc rtc = new Ds3231Rtc(bus);
    assertThrows(IOException.class, rtc::getDateTime);
  }

  private BusRegisterDevice clockBus() {
    BusRegisterDevice bus = new BusRegisterDevice(0x68);
    bus.seed(0, 0x30, 0x15, 0x09, 3, 0x21, 0x10, 0x26);
    return bus;
  }

}
