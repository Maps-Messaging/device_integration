package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimingData implements AbstractRegisterData {
  private boolean manual;
  private boolean highGain;
  private IntegrationTime integrationTime;
}
