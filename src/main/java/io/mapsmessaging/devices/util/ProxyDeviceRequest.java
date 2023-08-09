package io.mapsmessaging.devices.util;

import io.mapsmessaging.devices.DeviceRequest;
import lombok.Getter;

@Getter
public class ProxyDeviceRequest extends DeviceRequest {
  private final byte[] buf;

  public ProxyDeviceRequest(byte[] data) {
    buf = data;
  }
}
