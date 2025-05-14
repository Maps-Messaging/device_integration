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

package io.mapsmessaging.devices.impl;

public interface AddressableDevice {

  void close();

  int getBus();

  int write(int val);

  default int write(byte[] buffer) {
    return write(buffer, 0, buffer.length);
  }

  int write(byte[] buffer, int offset, int length);

  int writeRegister(int register, byte[] data);

  int read(byte[] buffer, int offset, int length);

  int readRegister(int register);

  int readRegister(int register, byte[] buffer, int offset, int length);

  int getDevice();

  int read();
}
  