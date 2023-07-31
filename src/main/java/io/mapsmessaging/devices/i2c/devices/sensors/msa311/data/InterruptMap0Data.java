package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

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
public class InterruptMap0Data implements RegisterData {
  private boolean orientationInterruptMappedToInt1;
  private boolean singleTapInterruptMappedToInt1;
  private boolean doubleTapInterruptMappedToInt1;
  private boolean activeInterruptMappedToInt1;
  private boolean freefallInterruptMappedToInt1;
}
