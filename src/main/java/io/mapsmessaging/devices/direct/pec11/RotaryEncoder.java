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

package io.mapsmessaging.devices.direct.pec11;

import com.pi4j.io.gpio.digital.DigitalOutput;

public class RotaryEncoder {
  // based on [lastEncoded][encoded] lookup
  private static final int[][] stateTable = {

      {0, 1, 1, -1},
      {-1, 0, 1, -1},
      {-1, 1, 0, -1},
      {-1, 1, 1, 0}
  };

  private final DigitalOutput inputA;
  private final DigitalOutput inputB;
  private final RotaryEncoderListener listener;

  private long encoderValue = 0;
  private int lastEncoded = 0;
  private boolean firstPass = true;


  public RotaryEncoder(DigitalOutput inA, DigitalOutput inB, RotaryEncoderListener listener, long initalValue) {
    this.listener = listener;
    encoderValue = initalValue;
    inputA = inA;
    inputB = inB;
    inputA.addListener(digitalStateChangeEvent -> {
      int stateA = digitalStateChangeEvent.state().getValue().intValue();
      int stateB = inputB.state().getValue().intValue();
      calcEncoderValue(stateA, stateB);
    });
  }

  public long getValue() {
    return encoderValue;
  }

  private void calcEncoderValue(int stateA, int stateB) {
    // converting the 2 pin value to single number to end up with 00, 01, 10 or 11
    int encoded = (stateA << 1) | stateB;
    if (firstPass) {
      firstPass = false;
    } else {
      // going up states, 01, 11
      // going down states 00, 10
      int state = stateTable[lastEncoded][encoded];
      encoderValue += state;
      if (listener != null) {
        if (state == -1) {
          listener.down(encoderValue);
        }
        if (state == 1) {
          listener.up(encoderValue);
        }
      }
    }
    lastEncoded = encoded;
  }
}
