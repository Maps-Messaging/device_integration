package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.ClockFrequency;
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
public class ControlData implements RegisterData {
  private boolean oscillatorEnabled;
  private boolean squareWaveEnabled;
  private boolean convertTemperatureEnabled;
  private ClockFrequency squareWaveFrequency;
  private boolean squareWaveInterruptEnabled;
  private boolean alarm1InterruptEnabled;
  private boolean alarm2InterruptEnabled;
}
