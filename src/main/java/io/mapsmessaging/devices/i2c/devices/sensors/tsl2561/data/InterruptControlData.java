package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptControl;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptPersistence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("InterruptControlData")
public class InterruptControlData implements RegisterData {
  private InterruptControl control;
  private InterruptPersistence persist;
}
