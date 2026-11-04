package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xDevice;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdcStartupRegressionTest {

  @Test
  void twelveBitSingleEndedFramePreservesLowestBitsOnChannelSeven() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, 0x0a, (byte) 0xbd}), 12, 8);
    assertEquals(2749, adc.readFromChannel(false, (short) 7));
    assertArrayEquals(new byte[]{7, (byte) 0xc0, 0}, requests.get(0));
  }

  @Test
  void twelveBitDifferentialFrameSelectsChannelThree() {
    List<byte[]> requests = new ArrayList<>();
    Mcp3y0xDevice adc = new Mcp3y0xDevice(spi(requests, new byte[]{0, 0x0f, (byte) 0xff}), 12, 4);
    assertEquals(4095, adc.readFromChannel(true, (short) 3));
    assertArrayEquals(new byte[]{4, (byte) 0xc0, 0}, requests.get(0));
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
