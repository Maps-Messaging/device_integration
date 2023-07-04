package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

public class Mcp3004Device extends Mcp3y0xDevice {


  public Mcp3004Device(Spi spi) {
    super(spi, 10, 4);
  }

  @Override
  public String getName() {
    return "MCP3004";
  }
}