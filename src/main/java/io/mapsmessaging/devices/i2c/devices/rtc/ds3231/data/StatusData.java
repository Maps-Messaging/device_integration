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
public class StatusData implements RegisterData {
  private boolean oscillatorStopped;
  private boolean enable32K;
  private boolean busy;
  private boolean alarm2FlagSet;
  private boolean alarm1FlagSet;

}
