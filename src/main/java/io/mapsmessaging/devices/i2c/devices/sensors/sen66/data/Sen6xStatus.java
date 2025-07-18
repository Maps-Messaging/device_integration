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

package io.mapsmessaging.devices.i2c.devices.sensors.sen66.data;


import java.util.BitSet;

public class Sen6xStatus {

  private final BitSet bits;

  public Sen6xStatus(int rawStatus) {
    this.bits = BitSet.valueOf(new long[]{rawStatus & 0xFFFFFFFFL});
  }

  public boolean isFanError()         { return bits.get(4); }
  public boolean isRhtError()         { return bits.get(6); }
  public boolean isGasError()         { return bits.get(7); }
  public boolean isCo2_2Error()       { return bits.get(9); }
  public boolean isHchoError()        { return bits.get(10); }
  public boolean isPmError()          { return bits.get(11); }
  public boolean isCo2_1Error()       { return bits.get(12); }
  public boolean isCompensationActive()  { return bits.get(15); }
  public boolean isSpeedWarning()     { return bits.get(21); }

  public boolean anyError() {
    return isFanError() || isRhtError() || isGasError() || isCo2_1Error() ||
        isCo2_2Error() || isHchoError() || isPmError();
  }

  @Override
  public String toString() {
    return "Sen6xStatus{" +
        "Fan=" + isFanError() +
        ", RH&T=" + isRhtError() +
        ", GAS=" + isGasError() +
        ", CO2-1=" + isCo2_1Error() +
        ", CO2-2=" + isCo2_2Error() +
        ", HCHO=" + isHchoError() +
        ", PM=" + isPmError() +
        ", SPEED-Warn=" + isSpeedWarning() +
        '}';
  }
}