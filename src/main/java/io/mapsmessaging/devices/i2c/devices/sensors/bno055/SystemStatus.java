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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;

public class SystemStatus {

  private final String[] StateString = {
      "Idle",
      "Error",
      "Initialise Peripherals",
      "System Initialisation",
      "Self Test",
      "Fusion Algorithm",
      "No Fusion",
      "Unknown"
  };

  private final String[] ErrorString = {
      "No error",
      "Peripheral initialization error",
      "System initialization error",
      "Self test result failed",
      "Register map value out of range",
      "Register map address out of range",
      "Register map write error",
      "BNO low power mode not available for selected operation mode",
      "Accelerometer power mode not available",
      "Fusion algorithm configuration error",
      "Sensor configuration error"
  };

  private final int system;
  private final int selftest;
  private final int error;

  protected SystemStatus(int system, int selfTest, int error) {
    this.system = system;
    selftest = selfTest;
    this.error = error;
  }

  public String getErrorString() {
    if (error >= ErrorString.length) {
      return "Unknown error string:" + error;
    }
    return ErrorString[error];
  }

  public Error getError() {
    switch (error) {
      case 0:
        return Error.NoError;
      case 1:
        return Error.Peripheral;
      case 2:
        return Error.SystemInit;
      case 3:
        return Error.SelfTest;
      case 4:
        return Error.ValueOutOfRange;
      case 5:
        return Error.AddressOutOfRange;
      case 6:
        return Error.MapWrite;
      case 7:
        return Error.LowPower;
      case 8:
        return Error.AccelPowerMode;
      case 9:
        return Error.FusionConfig;

      default:
        return Error.Unknown;
    }
  }

  public String getStateString() {
    if (error >= StateString.length) {
      return "Unknown state::" + error;
    }
    return StateString[error];
  }

  public State getState() {
    switch (system) {
      case 0:
        return State.Idle;
      case 1:
        return State.Error;
      case 2:
        return State.InitPeripherals;
      case 3:
        return State.SystemInit;
      case 4:
        return State.SelfTest;
      case 5:
        return State.Fusion;
      case 6:
        return State.NoFusion;
      default:
        return State.Unknown;
    }
  }

  public boolean selfTestAccelerometer() {
    return ((selftest & 0x1) != 0) || selftest == 0xf;
  }

  public boolean selfTestMagnetometer() {
    return ((selftest & 0x2) != 0) || selftest == 0xf;
  }

  public boolean selfTestGyroscope() {
    return ((selftest & 0x4) != 0) || selftest == 0xf;
  }

  public boolean selfTestMCU() {
    return ((selftest & 0x8) != 0) || selftest == 0xf;
  }

  public String toString() {
    String sb = "System Status:\n" + "\tState:" + getStateString() + "\n" +
        "\tError:" + getErrorString() + "\n" +
        "\tAccel:" + selfTestAccelerometer() + " Magnetometer:" + selfTestMagnetometer() + " Gyroscope:" + selfTestGyroscope() + " MCU:" + selfTestMCU() + "\n";
    return sb;
  }


  public enum State {
    Idle, Error, InitPeripherals, SystemInit, SelfTest, Fusion, NoFusion, Unknown
  }

  public enum Error {
    NoError, Peripheral, SystemInit, SelfTest, ValueOutOfRange, AddressOutOfRange,
    MapWrite, LowPower, AccelPowerMode, FusionConfig, Sensor, Unknown
  }

}