package io.mapsmessaging.devices.i2c.devices.storage.at24c.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Details {
  private String name;
  private int size;
}
