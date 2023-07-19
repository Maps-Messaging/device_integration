package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataReadyInterrupt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Control3Data implements AbstractRegisterData {

  private boolean interruptActive;
  private boolean pushPullDrainInterruptActive;
  private DataReadyInterrupt signalOnInterrupt;

}
