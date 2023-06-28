package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;

public class mcp3008Device extends mcp3y0xDevice {


  public mcp3008Device(Spi spi, DigitalOutput clientSelect) {
    super(spi, clientSelect, 10, 8);
  }

  @Override
  public String getName() {
    return "MCP3008";
  }

}