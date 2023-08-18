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

package io.mapsmessaging.devices.deviceinterfaces;

import java.io.IOException;

public interface Gpio {

  int getPins();

  boolean isOutput(int pin) throws IOException;

  void setOutput(int pin) throws IOException;

  void enableInterrupt(int pin) throws IOException;

  void disableInterrupt(int pin) throws IOException;

  int[] getInterrupted() throws IOException;

  void setHigh(int pin) throws IOException;

  void setLow(int pin) throws IOException;

  void setOnHigh(int pin) throws IOException;

  void setOnLow(int pin) throws IOException;

  void setInput(int pin) throws IOException;

  void enablePullUp(int pin) throws IOException;

  void disablePullUp(int pin) throws IOException;

  boolean isSet(int pin) throws IOException;

}
