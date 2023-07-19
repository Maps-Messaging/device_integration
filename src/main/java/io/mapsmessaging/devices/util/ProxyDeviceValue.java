package io.mapsmessaging.devices.util;

import io.mapsmessaging.devices.DeviceValue;
import lombok.Getter;

public class ProxyDeviceValue extends DeviceValue {

  @Getter
  private final byte[] buf;

  public ProxyDeviceValue(byte[] data){
    buf = data;
  }
}
