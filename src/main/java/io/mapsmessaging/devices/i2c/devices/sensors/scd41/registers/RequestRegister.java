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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.Request;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class RequestRegister extends Register {

  private static final AtomicLong counter = new AtomicLong(0);
  protected Request request;

  protected RequestRegister(I2CDevice sensor, String name, Request request) {
    super(sensor, (int) counter.getAndIncrement(), name);
    this.request = request;
  }

  @Override
  protected void reload() throws IOException {
    // No need for this in a request
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    // No need for this in a request
  }

  @Override
  public String toString(int maxLength) {
    return "";
  }

}

