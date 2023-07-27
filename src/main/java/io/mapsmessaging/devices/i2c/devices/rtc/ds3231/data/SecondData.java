package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SecondData implements AbstractRegisterData {
  private int seconds;
}
