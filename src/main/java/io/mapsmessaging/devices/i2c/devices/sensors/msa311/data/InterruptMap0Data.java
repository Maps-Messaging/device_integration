package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterruptMap0Data implements AbstractRegisterData {
  private boolean orientationInterruptMappedToInt1;
  private boolean singleTapInterruptMappedToInt1;
  private boolean doubleTapInterruptMappedToInt1;
  private boolean activeInterruptMappedToInt1;
  private boolean freefallInterruptMappedToInt1;
}
