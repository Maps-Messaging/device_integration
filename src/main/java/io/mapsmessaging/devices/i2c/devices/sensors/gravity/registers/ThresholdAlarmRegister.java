package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.GasSensor;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.AlarmType;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class ThresholdAlarmRegister extends CrcValidatingRegsiter {


  public ThresholdAlarmRegister(I2CDevice sensor ) {
    super(sensor, Command.SET_THRESHOLD_ALARMS);
  }

  public boolean setThresholdAlarm (int threshold, AlarmType alarmType) throws IOException {
    if (threshold == 0) {
      threshold = ((GasSensor)sensor).getSensorType().getThreshold();
    }
    byte[] buf = new byte[6];
    buf[1] = 0x1; // enable
    buf[2] = (byte) (threshold >> 8 & 0xff);
    buf[3] = (byte) (threshold & 0xff);
    buf[4] = alarmType.getValue();
    return sendBufferCommand(buf);
  }

  public boolean clearThresholdAlarm(AlarmType alarmType) throws IOException {
    byte[] buf = new byte[6];
    buf[1] = 0x0; // disable
    buf[2] = 0x0;
    buf[3] = (byte) 0xff;
    buf[4] = alarmType.getValue();
    return sendBufferCommand(buf);
  }

}
