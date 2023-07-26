package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatusData implements AbstractRegisterData {
  private List<Status> statusList;
}
