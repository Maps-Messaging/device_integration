package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ControlData implements AbstractRegisterData {
  private boolean powerOn;
}
