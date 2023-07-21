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
public class SwapPolarityData implements AbstractRegisterData {
  private boolean xPolaritySwapped;
  private boolean yPolaritySwapped;
  private boolean zPolaritySwapped;
  private boolean xYSwapped;
}
