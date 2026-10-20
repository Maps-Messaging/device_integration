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
