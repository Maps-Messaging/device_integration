package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.InterruptSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterruptSourceData implements AbstractRegisterData {
  private List<InterruptSource> interruptSources;
}
