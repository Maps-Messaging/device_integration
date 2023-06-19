package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.io.spi.Spi;

import java.io.IOException;

public class mcp3004Device  extends mcp3y0xDevice {


  public mcp3004Device(Spi spi) throws IOException {
    super(spi, 10, 4);
  }
}