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

package io.mapsmessaging.devices.i2c.devices.demo;

public class SimulatedIntValue {
  private int current;
  private int target;
  private final int min;
  private final int max;
  private final int step;

  public SimulatedIntValue(int min, int max, int initial, int step) {
    this.min = min;
    this.max = max;
    this.current = initial;
    this.target = randomTarget();
    this.step = step;
  }

  private int randomTarget() {
    return min + (int) (Math.random() * (max - min));
  }

  public int next() {
    if (Math.abs(current - target) < step) {
      target = randomTarget();
    }
    current += Integer.signum(target - current) * step;
    return current;
  }
}
