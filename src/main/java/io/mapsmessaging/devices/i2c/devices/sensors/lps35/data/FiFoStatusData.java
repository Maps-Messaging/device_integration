package io.mapsmessaging.devices.i2c.devices.sensors.lps35.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@Data
@AllArgsConstructor
@ToString
public class FiFoStatusData implements RegisterData {
  private final boolean hitThreshold;
  private final boolean isOverwritten;
  private final int size;

}
