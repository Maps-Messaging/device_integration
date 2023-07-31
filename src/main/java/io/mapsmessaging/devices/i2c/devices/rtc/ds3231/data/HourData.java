package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName("HourData")
public class HourData implements RegisterData {
  private boolean topSet;
  private boolean clock24Mode;
  private boolean pm;
  private int hours;
}
