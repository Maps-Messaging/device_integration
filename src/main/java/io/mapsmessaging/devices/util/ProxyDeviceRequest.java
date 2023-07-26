package io.mapsmessaging.devices.util;

import io.mapsmessaging.devices.DeviceRequest;
import lombok.Getter;

public class ProxyDeviceRequest extends DeviceRequest {
  @Getter
  private final byte[] buf;

  public ProxyDeviceRequest(byte[] data) {
    buf = data;
  }
}
