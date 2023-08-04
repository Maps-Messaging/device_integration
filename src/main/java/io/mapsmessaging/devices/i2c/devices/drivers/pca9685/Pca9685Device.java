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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685;

import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

@Getter
public class Pca9685Device extends I2CDevice implements Output {
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
    delay(5);
    preScaleRegister.setPWMFrequency(value);
    delay(5);
    mode1Register.setSleep(false);
    delay(5);
    mode1Register.restart();
  }

  public void setPWM(int channel, int on, int off) throws IOException {
    ledControlRegisters[channel].setRate(on, off);
  }

  public void setAllPWM(int on, int off) throws IOException {
    allLedControlRegisters.setRate(on, off);
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
  }
}
