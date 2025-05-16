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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

@Getter
public class Pca9685Device extends I2CDevice implements Resetable, Output {
  private final Mode1Register mode1Register;
  private final Mode2Register mode2Register;
  private final SubAddressRegister subAddressRegister1;
  private final SubAddressRegister subAddressRegister2;
  private final SubAddressRegister subAddressRegister3;
  private final SubAddressRegister allCallAddressRegister;
  private final LedControlRegister allLedControlRegisters;
  private final LedControlRegister[] ledControlRegisters;
  private final PreScaleRegister preScaleRegister;

  public Pca9685Device(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Pca9685Device.class));
    mode1Register = new Mode1Register(this);
    preScaleRegister = new PreScaleRegister(this);
    mode2Register = new Mode2Register(this);

    subAddressRegister1 = new SubAddressRegister(this, 2, "SUBADR1");
    subAddressRegister2 = new SubAddressRegister(this, 3, "SUBADR2");
    subAddressRegister3 = new SubAddressRegister(this, 4, "SUBADR3");
    allCallAddressRegister = new SubAddressRegister(this, 5, "ALLCALLADR");
    ledControlRegisters = new LedControlRegister[16];
    for (int x = 0; x < ledControlRegisters.length; x++) {
      ledControlRegisters[x] = new LedControlRegister(this, (6 + (x * 4)), "LED_" + x);
    }
    allLedControlRegisters = new LedControlRegister(this, 0xFA, "ALL_LED");
    initialise();
  }

  @Override
  public void close() {
    try {
      setAllPWM(0, 0);
      mode1Register.restart();
      mode1Register.setSleep(true);
    } catch (IOException e) {
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public void setPWMFrequency(float value) throws IOException {
    mode1Register.setSleep(true);
    preScaleRegister.setPWMFrequency(value);
    mode1Register.setSleep(false);
  }

  public void setPWM(int channel, int on, int off) throws IOException {
    ledControlRegisters[channel].setRate(on, off);
    mode1Register.restart();
  }

  public void setAllPWM(int on, int off) throws IOException {
    allLedControlRegisters.setRate(on, off);
    mode1Register.restart();
  }

  @Override
  public String getName() {
    return "PCA9685";
  }

  @Override
  public String getDescription() {
    return "PCA9685 16 port PWM controller";
  }

  private void initialise() throws IOException {
    mode1Register.reset();
    mode1Register.setAutoIncrement(true);
    mode1Register.restart();
  }

  @Override
  public void reset() throws IOException {
    initialise();
    mode1Register.enableAllCall(true);
    for (LedControlRegister ledControlRegister : ledControlRegisters) {
      ledControlRegister.setRate(0, 0);
    }
    setAllPWM(0, 0);
    mode1Register.restart();
    delay(5);
  }

  @Override
  public void softReset() throws IOException {
    setAllPWM(0, 0);
  }


  @Override
  public DeviceType getType() {
    return DeviceType.PWM;
  }
}
