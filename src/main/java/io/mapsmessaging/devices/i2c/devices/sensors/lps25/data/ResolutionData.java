package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.PressureAverage;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.TemperatureAverage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResolutionData implements AbstractRegisterData {
  private PressureAverage pressureAverage;
  private TemperatureAverage temperatureAverage;
}
