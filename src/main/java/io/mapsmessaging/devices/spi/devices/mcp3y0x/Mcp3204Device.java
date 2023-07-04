package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

public class Mcp3204Device extends Mcp3y0xDevice {


  public Mcp3204Device(Spi spi) {
    super(spi, 12, 4);
  }

  @Override
  public String getName() {
    return "MCP3204";
  }

}
