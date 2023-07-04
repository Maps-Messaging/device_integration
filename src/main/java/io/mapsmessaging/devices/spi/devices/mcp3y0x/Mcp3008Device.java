package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

public class Mcp3008Device extends Mcp3y0xDevice {


  public Mcp3008Device(Spi spi) {
    super(spi, 10, 8);
  }

  @Override
  public String getName() {
    return "MCP3008";
  }

}