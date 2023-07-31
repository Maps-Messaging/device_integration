package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.StatusData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusRegister extends SingleByteRegister {

  private static final byte STATUS_REGISTER = 0x27;

  private static final byte PRESSURE_OVERRUN = 0b00010000;
  private static final byte TEMPERATURE_OVERRUN = 0b00100000;
  private static final byte PRESSURE_DATA_AVAILABLE = 0b00000001;
  private static final byte TEMPERATURE_DATA_AVAILABLE = 0b00000010;

  public StatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, STATUS_REGISTER, "STATUS_REG");
    reload();
  }

  public Status[] getStatus() throws IOException {
    reload();
    List<Status> sourceList = new ArrayList<>();
    if ((registerValue & TEMPERATURE_OVERRUN) != 0) {
      sourceList.add(Status.TEMPERATURE_OVERRUN);
    }
    if ((registerValue & PRESSURE_OVERRUN) != 0) {
      sourceList.add(Status.PRESSURE_OVERRUN);
    }
    if ((registerValue & TEMPERATURE_DATA_AVAILABLE) != 0) {
      sourceList.add(Status.TEMPERATURE_DATA_AVAILABLE);
    }
    if ((registerValue & PRESSURE_DATA_AVAILABLE) != 0) {
      sourceList.add(Status.PRESSURE_DATA_AVAILABLE);
    }
    return sourceList.toArray(new Status[]{});
  }

  @Override
  public RegisterData toData() throws IOException {
    return new StatusData(Arrays.asList(getStatus()));
  }


}