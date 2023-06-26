package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

import java.io.IOException;

public class mcp3204Device extends mcp3y0xDevice {


  public mcp3204Device(Spi spi) {
    super(spi, 12, 4);
  }

  @Override
  public String getName() {
    return "MCP3204";
  }

}
