package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterruptControlData implements AbstractRegisterData {
  private boolean latchInterruptEnabled;
  private boolean interruptOnLowEnabled;
  private boolean interruptOnHighEnabled;
}
