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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.register.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.BooleanSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Mcp23017Device extends I2CDevice implements Sensor, Resetable, Gpio {

  private final IoDirectionRegister ioDir;
  private final InputPolarityRegister ipol;
  private final InterruptControlRegister gpIntEn;
  private final DefaultValueRegister defVal;
  private final InterruptOnChangeRegister intCon;
  private final PullupResisterRegister gppu;
  private final InterruptFlagRegister intf;
  private final InterruptCaptureRegister intCap;
  private final GpioPortRegister gpio;
  private final OutputLatchRegister olat;
  private final ExpanderConfigurationRegister iocon;
  private final List<SensorReading<?>> readings;

  public Mcp23017Device(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Mcp23017Device.class));
    ioDir = new IoDirectionRegister(this);
    ipol = new InputPolarityRegister(this);
    gpIntEn = new InterruptControlRegister(this);
    defVal = new DefaultValueRegister(this);
    intCon = new InterruptOnChangeRegister(this);
    iocon = new ExpanderConfigurationRegister(this, (byte) 0xA);
    gppu = new PullupResisterRegister(this);
    intf = new InterruptFlagRegister(this);
    intCap = new InterruptCaptureRegister(this);
    gpio = new GpioPortRegister(this);
    olat = new OutputLatchRegister(this);
    reset();
    List<SensorReading<?>> sensorReadings = new ArrayList<>();

    for (int pin = 0; pin < 16; pin++) {
      final int p = pin;
      sensorReadings.add(new BooleanSensorReading(
          "gpio_" + p,
          "",
          "Pin " + p + " digital state (0=Low, 1=High)",
          true,
          true,
          () -> isSet(p)
      ));
    }

    this.readings = sensorReadings;

  }

  public void reset() throws IOException {
    iocon.clear();
    ioDir.setAll();
    ipol.clearAll();
    gpIntEn.clearAll();
    intCon.clearAll();
    defVal.clearAll();
    gppu.clearAll();
    intf.clearAll();
    softReset();
  }

  @Override
  public void softReset() throws IOException {
    iocon.setSequential(true);
    iocon.setMirror(true);
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


  @Override
  public int getPins() {
    return 16; // hardwired since this is what it is
  }

  @Override
  public boolean isOutput(int pin) throws IOException {
    return !ioDir.get(pin);
  }

  @Override
  public void setOutput(int pin) throws IOException {
    ioDir.clear(pin);
  }

  @Override
  public void setInput(int pin) throws IOException {
    ioDir.set(pin);
  }

  @Override
  public void setHigh(int pin) throws IOException {
    gpio.set(pin);
  }

  @Override
  public void setLow(int pin) throws IOException {
    gpio.clear(pin);
  }

  @Override
  public void setOnHigh(int pin) throws IOException {
    defVal.set(pin);
  }

  @Override
  public void setOnLow(int pin) throws IOException {
    defVal.clear(pin);
  }

  @Override
  public boolean isSet(int pin) throws IOException {
    return gpio.get(pin);
  }

  @Override
  public void enableInterrupt(int pin) throws IOException {
    gpIntEn.set(pin);
  }

  @Override
  public void disableInterrupt(int pin) throws IOException {
    gpIntEn.clear(pin);
  }

  @Override
  public int[] getInterrupted() throws IOException {
    return intf.getAllSet();
  }

  @Override
  public void enablePullUp(int pin) throws IOException {
    gppu.set(pin);
  }

  @Override
  public void disablePullUp(int pin) throws IOException {
    gppu.clear(pin);
  }

  @Override
  public DeviceType getType() {
    return DeviceType.GPIO;
  }
}