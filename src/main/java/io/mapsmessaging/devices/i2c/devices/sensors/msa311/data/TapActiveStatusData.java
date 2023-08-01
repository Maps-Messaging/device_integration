package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.TapActiveStatus;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TapActiveStatusData implements RegisterData {
  private List<TapActiveStatus> tapActiveStatus;
}
