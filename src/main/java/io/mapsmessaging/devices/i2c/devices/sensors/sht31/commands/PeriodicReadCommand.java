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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands;
public class PeriodicReadCommand extends Command {
  private final Repeatability repeatability;
  private final Mps mps;

  public PeriodicReadCommand(Repeatability repeatability, Mps mps) {
    super((mps.getMsb() << 8) | mps.getLsb(repeatability), 0, 0);
    this.repeatability = repeatability;
    this.mps = mps;
  }

  public ReadDataCommand getReadCommand() {
    return new ReadDataCommand(repeatability);
  }

  public Repeatability getRepeatability() {
    return repeatability;
  }

  public Mps getMps() {
    return mps;
  }
}
