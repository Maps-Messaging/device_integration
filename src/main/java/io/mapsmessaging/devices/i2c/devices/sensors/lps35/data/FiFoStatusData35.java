package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FiFoStatusData35 implements RegisterData {
  private boolean hitThreshold;
  private boolean isOverwritten;
  private int size;

}
