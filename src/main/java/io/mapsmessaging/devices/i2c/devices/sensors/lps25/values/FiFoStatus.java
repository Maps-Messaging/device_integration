package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FiFoStatus {
  private boolean hitThreshold;
  private boolean isOverwritten;
  private int size;
}
