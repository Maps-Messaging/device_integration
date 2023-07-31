package io.mapsmessaging.devices.i2c.devices.sensors.msa311.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@Getter
@AllArgsConstructor
@ToString
public class PartIdData implements RegisterData {
  private final int id;
}

