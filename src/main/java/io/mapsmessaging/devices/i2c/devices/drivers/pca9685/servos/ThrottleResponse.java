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

public class ThrottleResponse implements AngleResponse {

    private final int myLowBound;
    private final int myServoRange;

    public ThrottleResponse(short lowBound, short highBound) {
        myLowBound = lowBound;
        myServoRange = highBound - lowBound;
    }

    public float getMin() {
        return 0.0f;
    }

    public float getMax() {
        return 100.0f;
    }

    public float getIdle() {
        return 0.0f;
    }

    @Override
    public float getResponse(float angle) {
        if (angle < 0.0f) angle = 0.0f;
        if (angle > 100.0f) angle = 100.0f;

        float pos = (100 - angle) / 100;
        pos = myServoRange * pos + myLowBound;
        return pos;
    }
}