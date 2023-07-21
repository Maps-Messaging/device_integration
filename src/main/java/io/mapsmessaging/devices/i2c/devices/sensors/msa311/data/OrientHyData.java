package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientBlocking;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class OrientHyData implements AbstractRegisterData {
  private int orientHysteresis;
  private OrientBlocking orientBlocking;
  private OrientMode orientMode;
}
