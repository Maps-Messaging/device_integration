package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class InterruptSet0Data implements AbstractRegisterData {
  private boolean orientInterruptEnabled;
  private boolean singleTapInterruptEnabled;
  private boolean doubleTapInterruptEnabled;
  private boolean activeInterruptEnabledZ;
  private boolean activeInterruptEnabledY;
  private boolean activeInterruptEnabledX;
}
