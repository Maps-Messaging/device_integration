package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm1Settings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Alarm1SettingsData implements AbstractRegisterData {
  private Alarm1Settings alarmSettings;
}