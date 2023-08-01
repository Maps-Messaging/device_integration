package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeIdResolver(value = TypeNameResolver.class)
public class HourData implements RegisterData {
  private boolean topSet;
  private boolean clock24Mode;
  private boolean pm;
  private int hours;
}
