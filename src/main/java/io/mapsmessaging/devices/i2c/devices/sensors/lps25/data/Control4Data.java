package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Control4Data implements RegisterData {
  private boolean fifoEmptyInterruptEnabled;
  private boolean fifoWatermarkInterruptEnabled;
  private boolean fifoOverrunInterruptEnabled;
  private boolean dataReadyInterrupt;
}
