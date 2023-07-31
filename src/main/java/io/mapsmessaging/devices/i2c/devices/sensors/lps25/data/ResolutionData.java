package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.PressureAverage;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.TemperatureAverage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResolutionData implements RegisterData {
  private PressureAverage pressureAverage;
  private TemperatureAverage temperatureAverage;
}
