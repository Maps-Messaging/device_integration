package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@AllArgsConstructor
@ToString
public class FiFoStatusData implements RegisterData {
  private final boolean hitThreshold;
  private final boolean isOverwritten;
  private final int size;

}
