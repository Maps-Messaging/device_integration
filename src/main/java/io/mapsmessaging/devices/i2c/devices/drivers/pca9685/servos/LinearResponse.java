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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.servos;

public class LinearResponse implements AngleResponse {

  private final int myLowBound;
  private final int myServoRange;

  private final float myMinAngle;
  private final float myMaxAngle;
  private final float myRange;


  public LinearResponse(short lowBound, short highBound, float minAngle, float maxAngle) {
    myLowBound = lowBound;
    myServoRange = highBound - lowBound;

    myMinAngle = minAngle;
    myMaxAngle = maxAngle;
    myRange = myMaxAngle - myMinAngle;
  }

  public float getMin() {
    return myMinAngle;
  }

  public float getMax() {
    return myMaxAngle;
  }

  public float getIdle() {
    return (0.0f) / 2.0f;
  }

  @Override
  public float getResponse(float angle) {
    if (angle < myMinAngle)
      angle = myMinAngle;
    if (angle > myMaxAngle)
      angle = myMaxAngle;

    float pos = (myMaxAngle - angle) / myRange;
    pos = myServoRange * pos + myLowBound;
    return pos;
  }
}