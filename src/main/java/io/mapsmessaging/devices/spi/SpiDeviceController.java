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

package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;
import io.mapsmessaging.devices.DeviceController;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class SpiDeviceController implements DeviceController {
  private boolean raiseExceptionOnError = false;

  public abstract SpiDeviceController mount(Context pi4j, Map<String, String> config);

  public Spi createDevice(Context pi4j, String name, String id, int spiBus, SpiChipSelect chipSelect, SpiMode mode) {
    var spiConfig = Spi.newConfigBuilder(pi4j)
        .id(id)
        .name(name)
        .bus(spiBus)
        .chipSelect(chipSelect) // not used
        .mode(mode)
        .provider("pigpio-spi")
        .build();
    System.err.println("Creating new device " + id);
    return pi4j.create(spiConfig);
  }

  public DigitalOutput createClientSelect(Context pi4j, String id, String name, int address, DigitalState initialState, DigitalState shutdownState) {
    // required all configs
    var outputConfig = DigitalOutput.newConfigBuilder(pi4j)
        .id(id)
        .name(name)
        .address(address)
        .shutdown(shutdownState)
        .initial(initialState)
        .provider("pigpio-digital-output");
    return pi4j.create(outputConfig);
  }

  public void closeDevice(Spi device) {
    device.close();
  }

  protected SpiChipSelect getChipSelect(int chipSelectInt) {
    SpiChipSelect chipSelect;
    switch (chipSelectInt) {
      case 2:
        chipSelect = SpiChipSelect.CS_2;
        break;

      case 1:
        chipSelect = SpiChipSelect.CS_1;
        break;

      default:
        chipSelect = SpiChipSelect.CS_0;
    }
    return chipSelect;
  }

  protected SpiMode getMode(int spiModeInt) {
    switch (spiModeInt) {
      case 1:
        return SpiMode.MODE_1;
      case 2:
        return SpiMode.MODE_2;
      case 3:
        return SpiMode.MODE_3;
      default:
        return SpiMode.MODE_0;

    }
  }
}
