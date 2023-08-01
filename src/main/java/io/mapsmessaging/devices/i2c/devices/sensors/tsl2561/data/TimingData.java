package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeIdResolver(value = TypeNameResolver.class)
public class TimingData implements RegisterData {
  private boolean manual;
  private boolean highGain;
  private IntegrationTime integrationTime;
}
