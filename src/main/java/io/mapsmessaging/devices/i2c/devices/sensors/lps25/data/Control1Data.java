package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
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
public class Control1Data implements RegisterData {
  private boolean powerDownMode;
  private DataRate dataRate;
  private boolean interruptGenerationEnabled;
  private boolean blockUpdateSet;
}
