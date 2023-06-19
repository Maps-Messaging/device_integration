package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

import java.io.IOException;

public class mcp3208Device extends mcp3y0xDevice {


  public mcp3208Device(Spi spi) throws IOException {
    super(spi, 12, 8);
  }
}
