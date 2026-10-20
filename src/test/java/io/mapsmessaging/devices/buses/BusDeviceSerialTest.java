package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.serial.devices.sensors.sen0640.Sen0640Sensor;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class BusDeviceSerialTest {
  @Test void solarReadingEmitsCrcProtectedHoldingRegisterRequestAndDecodesBigEndianReply() throws Exception {
    BusSerialPort port = new BusSerialPort((byte) 1, (byte) 3, (byte) 2, (byte) 2, (byte) 188, (byte) 184, (byte) 149);
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    assertEquals(700, sensor.getSolarRadiation());
    assertArrayEquals(new byte[]{1, 3, 0, 0, 0, 1, (byte) 0x84, 0x0a}, port.requests.get(0));
  }
  @Test void deviationWriteRequiresMatchingRegisterValueEcho() throws Exception {
    BusSerialPort port = new BusSerialPort((byte) 1, (byte) 6, (byte) 0, (byte) 82, (byte) 0, (byte) 100, (byte) 41, (byte) 240);
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    sensor.setDeviation(100);
    assertArrayEquals(new byte[]{1, 6, 0, 82, 0, 100, 41, (byte) 240}, port.requests.get(0));
    port.response = new byte[]{1, 6, 0, 82, 0, 101, (byte) 232, 48};
    assertThrows(IOException.class, () -> sensor.setDeviation(100));
  }
  @Test void badCrcIsRejectedInsteadOfPublishingCorruptValue() throws Exception {
    BusSerialPort port = new BusSerialPort((byte) 1, (byte) 3, (byte) 2, (byte) 2, (byte) 188, (byte) 0, (byte) 0);
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    assertThrows(IOException.class, sensor::getSolarRadiation);
  }
  @Test void crcValidSolarValueAbovePhysicalRangeIsRejected() throws Exception {
    BusSerialPort port = new BusSerialPort((byte) 1, (byte) 3, (byte) 2, (byte) 7, (byte) 9, (byte) 122, (byte) 114);
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    assertThrows(IOException.class, sensor::getSolarRadiation);
  }
  @Test void partialWriteAndTransportReadFailureAreReported() throws Exception {
    BusSerialPort port = new BusSerialPort();
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    port.writeLimit = 7;
    assertThrows(IOException.class, sensor::getSolarRadiation);
    port.writeLimit = Integer.MAX_VALUE;
    port.readResult = -1;
    assertThrows(IOException.class, sensor::getSolarRadiation);
  }
  @Test void missingResponseExpiresAndClosedPortCannotSendAnotherRequest() throws Exception {
    BusSerialPort port = new BusSerialPort();
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    sensor.setResponseTimeout(Duration.ofMillis(1));
    assertThrows(IOException.class, sensor::getSolarRadiation);
    sensor.close();
    assertThrows(IOException.class, sensor::getSolarRadiation);
    assertEquals(1, port.requests.size());
  }
  @Test void invalidDeviationIsRejectedBeforeAnyUartWrite() throws Exception {
    BusSerialPort port = new BusSerialPort();
    Sen0640Sensor sensor = new Sen0640Sensor(port);
    assertThrows(IllegalArgumentException.class, () -> sensor.setDeviation(-1));
    assertThrows(IllegalArgumentException.class, () -> sensor.setDeviation(1801));
    assertTrue(port.requests.isEmpty());
  }
  @Test void openingFailureIsReportedBeforeSensorCanBeUsed() {
    BusSerialPort port = new BusSerialPort();
    port.canOpen = false;
    assertThrows(IOException.class, () -> new Sen0640Sensor(port));
  }
}
