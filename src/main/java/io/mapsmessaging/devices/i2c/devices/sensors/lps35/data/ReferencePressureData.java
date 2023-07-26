package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReferencePressureData implements AbstractRegisterData {
  private int reference;
}
