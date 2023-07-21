package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class OrientationData implements AbstractRegisterData {
  private OrientationStatus orientation;
}
