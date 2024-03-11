package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SerialNumberRequest extends Request {
  public SerialNumberRequest(AddressableDevice device) {
    super(1, 0x3682, 9, device);
  }

  public int getSerialNumber(){
    byte[] response = getResponse();
    int val = 0;
    if(generateCrc(response, 0) == response[2]){
      val = response[0] << 8 | (response[1] & 0xff);
    }
    if(generateCrc(response, 3) == response[5]){
      int raw = (response[3] & 0xFF) << 8 | (response[4] & 0xFF);
      val = val << 16 | raw & 0xffff;
    }
    if(generateCrc(response, 6) == response[8]){
      int raw = response[6] << 8 | (response[7] & 0xff);
      val = val << 16 | raw & 0xffff;
    }
    return val;
  }
}
