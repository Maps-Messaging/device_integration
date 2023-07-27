package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.ClockFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ControlData implements AbstractRegisterData {
  private boolean oscillatorEnabled;
  private boolean squareWaveEnabled;
  private boolean convertTemperatureEnabled;
  private ClockFrequency squareWaveFrequency;
  private boolean squareWaveInterruptEnabled;
  private boolean alarm1InterruptEnabled;
  private boolean alarm2InterruptEnabled;
}
