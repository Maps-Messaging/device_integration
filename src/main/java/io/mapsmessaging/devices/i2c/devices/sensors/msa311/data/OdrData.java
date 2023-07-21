package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class OdrData implements AbstractRegisterData {
  private Odr odr;
  private boolean xAxisDisabled;
  private boolean yAxisDisabled;
  private boolean zAxisDisabled;
}
