package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
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
public class Control4Data implements RegisterData {
  private boolean fifoEmptyInterruptEnabled;
  private boolean fifoWatermarkInterruptEnabled;
  private boolean fifoOverrunInterruptEnabled;
  private boolean dataReadyInterrupt;
}
