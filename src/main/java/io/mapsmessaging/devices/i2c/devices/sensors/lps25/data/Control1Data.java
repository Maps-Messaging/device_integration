package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Control1Data implements AbstractRegisterData {
  private boolean powerDownMode;
  private DataRate dataRate;
  private boolean interruptGenerationEnabled;
  private boolean blockUpdateSet;
}
