package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class FiFoStatusData implements AbstractRegisterData {
  private final boolean hitThreshold;
  private final boolean isOverwritten;
  private final int size;

}
