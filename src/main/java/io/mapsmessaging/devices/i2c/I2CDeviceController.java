/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.DeviceController;
import lombok.Getter;

import java.io.IOException;

public abstract class I2CDeviceController implements DeviceController {

  @Getter
  private final int mountedAddress;

  protected I2CDeviceController() {
    this(null);
  }

  protected I2CDeviceController(I2C device) {
    if (device != null) {
      mountedAddress = device.getDevice();
    } else {
      mountedAddress = -1;
    }
  }

  public abstract I2CDeviceController mount(I2C device) throws IOException;

  public abstract int[] getAddressRange();

  public abstract boolean detect();

}
