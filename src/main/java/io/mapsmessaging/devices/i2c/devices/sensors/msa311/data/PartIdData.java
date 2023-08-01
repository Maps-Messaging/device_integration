package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Getter
@AllArgsConstructor
@ToString
public class PartIdData implements RegisterData {
  private final int id;
}

