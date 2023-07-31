package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("TimingData")
public class TimingData implements RegisterData {
  private boolean manual;
  private boolean highGain;
  private IntegrationTime integrationTime;
}
