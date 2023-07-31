package io.mapsmessaging.devices.i2c.devices.storage.at24c.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.values.ActionType;
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
public class AT24CnnCommand {
  private ActionType action;
  private int address;
  private byte[] data;
  private int length;
}