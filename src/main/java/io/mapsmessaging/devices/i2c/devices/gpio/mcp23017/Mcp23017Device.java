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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017;

import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.register.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

@Getter
public class Mcp23017Device extends I2CDevice implements Sensor, Resetable {

  private final IoDirectionRegister ioDir;
  @Getter
  private final InputPolarityRegister ipol;
  @Getter
  private final InterruptControlRegister gpIntEn;
  @Getter
  private final DefaultValueRegister defVal;
  @Getter
  private final InterruptOnChangeRegister intCon;
  @Getter
  private final PullupResisterRegister gppu;
  @Getter
  private final InterruptFlagRegister intf;
  @Getter
  private final InterruptCaptureRegister intCap;
  @Getter
  private final GpioPortRegister gpio;
  @Getter
  private final OutputLatchRegister olat;
  @Getter
  private final ExpanderConfigurationRegister ioconA;
  @Getter
  private final ExpanderConfigurationRegister ioconB;

  public Mcp23017Device(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Mcp23017Device.class));
    ioDir = new IoDirectionRegister(this);
    ipol = new InputPolarityRegister(this);
    gpIntEn = new InterruptControlRegister(this);
    defVal = new DefaultValueRegister(this);
    intCon = new InterruptOnChangeRegister(this);
    ioconA = new ExpanderConfigurationRegister(this, (byte) 0xA);
    ioconB = new ExpanderConfigurationRegister(this, (byte) 0xB);
    gppu = new PullupResisterRegister(this);
    intf = new InterruptFlagRegister(this);
    intCap = new InterruptCaptureRegister(this);
    gpio = new GpioPortRegister(this);
    olat = new OutputLatchRegister(this);
    reset();
  }

  public void reset() throws IOException {
    softReset();
  }

  @Override
  public void softReset() throws IOException {
    ioconA.setSequential(true);
    ioconB.setSequential(true);
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public String getName() {
    return "MCP23017";
  }

  @Override
  public String getDescription() {
    return "GPIO 16 pin extender";
  }


}