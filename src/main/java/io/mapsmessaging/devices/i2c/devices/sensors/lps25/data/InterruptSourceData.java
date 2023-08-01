package io.mapsmessaging.devices.i2c.devices.sensors.lps25.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.InterruptSource;
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
public class InterruptSourceData implements RegisterData {
  private List<InterruptSource> interruptSources;
}
