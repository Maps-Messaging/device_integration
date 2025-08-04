/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.TapActiveStatusData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.TapActiveStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TapActiveStatusRegister extends SingleByteRegister {

  public TapActiveStatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xb, "Tap_Active_Status");
  }

  public List<TapActiveStatus> getTapActiveStatus() throws IOException {
    List<TapActiveStatus> list = new ArrayList<>();
    reload();
    for (TapActiveStatus taps : TapActiveStatus.values()) {
      if ((taps.getMask() & registerValue) != 0) {
        list.add(taps);
      }
    }
    return list;
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof TapActiveStatusData) {
      TapActiveStatusData data = (TapActiveStatusData) input;
      List<TapActiveStatus> tapStatus = data.getTapActiveStatus();
      int value = 0;
      for (TapActiveStatus status : tapStatus) {
        value |= status.getMask();
      }
      setControlRegister(0xFF, value);
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new TapActiveStatusData(getTapActiveStatus());
  }

}
