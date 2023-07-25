package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptControl;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptPersistence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterruptControlData implements AbstractRegisterData {
  private InterruptControl control;
  private InterruptPersistence persist;
}
