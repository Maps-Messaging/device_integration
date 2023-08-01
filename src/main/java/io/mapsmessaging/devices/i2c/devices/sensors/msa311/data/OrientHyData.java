package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientBlocking;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.OrientMode;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class OrientHyData implements RegisterData {
  private int orientHysteresis;
  private OrientBlocking orientBlocking;
  private OrientMode orientMode;
}
