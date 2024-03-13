package io.mapsmessaging.devices.i2c.devices.sensors.bme688.data;


import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.HeaterStep;
import io.mapsmessaging.devices.io.TypeNameResolver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonTypeIdResolver(value = TypeNameResolver.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ControlGas1 implements RegisterData {
  private boolean runGas;
  private HeaterStep heaterStep;
}
