/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.GasSensor;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.AlarmType;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class ThresholdAlarmRegister extends CrcValidatingRegister {


  public ThresholdAlarmRegister(I2CDevice sensor) {
    super(sensor, Command.SET_THRESHOLD_ALARMS);
  }

  public boolean setThresholdAlarm(int threshold, AlarmType alarmType) throws IOException {
    if (threshold == 0) {
      threshold = ((GasSensor) sensor).getSensorType().getThreshold();
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
