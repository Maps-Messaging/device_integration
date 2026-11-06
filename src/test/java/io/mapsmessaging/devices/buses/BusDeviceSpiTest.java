package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xDevice;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusDeviceSpiTest {
  @Test void singleEndedChannelSevenUsesMcp3008FrameAndMasksStatusBits() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, (byte) 0xfe, (byte) 0xaa}), 10, 8);
    assertEquals(682, adc.readFromChannel(false, (short) 7));
    assertArrayEquals(new byte[]{1, (byte) 0xf0, 0}, requests.get(0));
  }
  @Test void differentialChannelThreeClearsSingleEndedBitAndDecodesFullScale() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, 3, (byte) 0xff}), 10, 4);
    assertEquals(1023, adc.readFromChannel(true, (short) 3));
    assertArrayEquals(new byte[]{1, 0x30, 0}, requests.get(0));
  }
  @Test void channelBeyondConfiguredInputsDoesNotTouchSpi() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[3]), 10, 4);
    assertEquals(-1, adc.readFromChannel(false, (short) 4));
    assertTrue(requests.isEmpty());
  }
  @Test void mcp3208ChannelSevenUsesTwelveBitFrameAndDecodesFullScale() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, 0x0f, (byte) 0xff}), 12, 8);
    assertEquals(4095, adc.readFromChannel(false, (short) 7));
    assertArrayEquals(new byte[]{0x07, (byte) 0xc0, 0}, requests.get(0));
  }

  @Test void mcp3204DifferentialChannelThreeUsesExpectedFrame() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, 0x08, 0}), 12, 4);
    assertEquals(2048, adc.readFromChannel(true, (short) 3));
    assertArrayEquals(new byte[]{0x04, (byte) 0xc0, 0}, requests.get(0));
  }

  @Test void negativeChannelNeverTouchesSpiAndUnsupportedVariantsAreRejected() {
    List<byte[]> requests = new ArrayList<>();
    Spi port = spi(requests, new byte[3]);
    Mcp3y0xDevice adc = new Mcp3y0xDevice(port, 10, 4);
    assertEquals(-1, adc.readFromChannel(false, (short) -1));
    assertTrue(requests.isEmpty());
    assertThrows(IllegalArgumentException.class, () -> new Mcp3y0xDevice(port, 11, 4));
    assertThrows(IllegalArgumentException.class, () -> new Mcp3y0xDevice(port, 12, 6));
  }

  private Spi spi(List<byte[]> requests, byte[] reply) {
    return (Spi) Proxy.newProxyInstance(Spi.class.getClassLoader(), new Class<?>[]{Spi.class}, (proxy, method, args) -> {
      if (method.getName().equals("transfer") && args.length == 2 && args[0] instanceof byte[] && args[1] instanceof byte[]) {
        requests.add(((byte[]) args[0]).clone());
        System.arraycopy(reply, 0, (byte[]) args[1], 0, reply.length);
        return method.getReturnType() == int.class ? reply.length : null;
      }
      throw new AssertionError("Unexpected SPI operation: " + method.getName());
    });
  }
}
