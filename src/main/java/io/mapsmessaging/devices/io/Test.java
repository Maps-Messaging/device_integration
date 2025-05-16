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

package io.mapsmessaging.devices.io;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.AgingRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.AlarmDayRegister;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.util.Map;

public class Test {

  public static void main(String[] args) throws Exception {
    SerialisationHelper helper = new SerialisationHelper();


    TestDevice testDevice = new TestDevice(new Addressable(), LoggerFactory.getLogger(TestDevice.class));
    RegisterMap registerMap = new RegisterMap();
    registerMap.addRegister(new AgingRegister(testDevice));
    registerMap.addRegister(new AlarmDayRegister(testDevice, 10, "day"));

    Map<Integer, RegisterData> map = registerMap.getData();

    // serialize
    byte[] json = helper.serialise(map);
    System.out.println(new String(json));

    // deserialize
    Map<Integer, RegisterData> map2 = helper.deserialise(json);
    System.out.println(map2);
  }

  private static class Addressable implements AddressableDevice {

    @Override
    public void close() {

    }

    @Override
    public int getBus() {
      return 1;
    }

    @Override
    public int write(int val) {
      return 0;
    }

    @Override
    public int write(byte[] buffer, int offset, int length) {
      return 0;
    }

    @Override
    public int writeRegister(int register, byte[] data) {
      return 0;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
      return 0;
    }

    @Override
    public int readRegister(int register) {
      return 0;
    }

    @Override
    public int readRegister(int register, byte[] buffer, int offset, int length) {
      return 0;
    }

    @Override
    public int getDevice() {
      return 0;
    }

    @Override
    public int read() {
      return 0;
    }
  }

  private static class TestDevice extends I2CDevice {

    protected TestDevice(AddressableDevice device, Logger logger) {
      super(device, logger);
    }

    @Override
    public String getName() {
      return "test";
    }

    @Override
    public String getDescription() {
      return "test";
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public DeviceType getType() {
      return DeviceType.SENSOR;
    }

  }
}
