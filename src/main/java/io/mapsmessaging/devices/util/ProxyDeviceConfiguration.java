package io.mapsmessaging.devices.util;

import io.mapsmessaging.devices.DeviceConfiguration;
import lombok.Getter;

public class ProxyDeviceConfiguration extends DeviceConfiguration {

  @Getter
  private final byte[] buf;

  public ProxyDeviceConfiguration(byte[] data) {
    buf = data;
  }
}
