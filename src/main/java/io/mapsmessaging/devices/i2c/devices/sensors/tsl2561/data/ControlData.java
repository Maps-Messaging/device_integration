package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("ControlData")
public class ControlData implements RegisterData {
  private boolean powerOn;
}
