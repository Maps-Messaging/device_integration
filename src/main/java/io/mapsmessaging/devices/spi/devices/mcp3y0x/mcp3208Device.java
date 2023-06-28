package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;

public class mcp3208Device extends mcp3y0xDevice {


  public mcp3208Device(Spi spi, DigitalOutput clientSelect) {
    super(spi, clientSelect, 12, 8);
  }

  @Override
  public String getName() {
    return "MCP3208";
  }

}
