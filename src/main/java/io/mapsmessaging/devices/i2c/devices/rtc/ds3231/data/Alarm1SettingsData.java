package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm1Settings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("Alarm1SettingsData")
public class Alarm1SettingsData implements RegisterData {
  private Alarm1Settings alarmSettings;
}