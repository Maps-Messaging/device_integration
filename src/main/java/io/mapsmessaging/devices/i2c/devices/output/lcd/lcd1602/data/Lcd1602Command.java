package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Lcd1602Command {
  private ActionType action;
  private int address;
  private byte[] data;
  private int length;
}