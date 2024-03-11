package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

import java.util.Arrays;

public class SetASCERequest extends Request {

  public SetASCERequest(AddressableDevice device) {
    super(1, 0x2416, 0, device);
  }

  public void setASCEState(boolean flag){
    byte[] buf = new byte[5];
    Arrays.fill(buf, (byte)0);
    if(flag){
      buf[2] = 1;
    }
    buf[4] = generateCrc(buf, 2);
    buf[0] = getCommand()[0];
    buf[1] = getCommand()[1];
    sendAndWaitForResponse(buf);
  }
}
